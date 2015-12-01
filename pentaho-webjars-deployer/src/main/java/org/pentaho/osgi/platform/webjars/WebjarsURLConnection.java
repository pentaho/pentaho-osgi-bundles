/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2014 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.osgi.platform.webjars;


import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FilenameUtils;

import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by nbaker on 9/6/14.
 */
public class WebjarsURLConnection extends URLConnection {

  public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool( 5, new ThreadFactory() {
    @Override
    public Thread newThread( Runnable r ) {
      Thread thread = Executors.defaultThreadFactory().newThread( r );
      thread.setDaemon( true );
      thread.setName( "WebjarsURLConnection pool" );
      return thread;
    }
  } );

  private final JSONParser parser = new JSONParser();

  private static final String MANIFEST_MF = "MANIFEST.MF";
  private static final String PENTAHO_RJS_LOCATION = "META-INF/js/require.json";

  private static final String WEBJARS_REQUIREJS_NAME = "webjars-requirejs.js";
  private static final Pattern MODULE_PATTERN =
      Pattern.compile( "META-INF/resources/webjars/([^/]+)/([^/]+)/" + WEBJARS_REQUIREJS_NAME );

  private static final String POM_NAME = "pom.xml";
  private static final Pattern POM_PATTERN =
      Pattern.compile( "META-INF/maven/org.webjars([^/]*)/([^/]+)/" + POM_NAME );

  private static final String BOWER_NAME = "bower.json";
  private static final Pattern BOWER_PATTERN =
      Pattern.compile( "META-INF/resources/webjars/([^/]+)/([^/]+)/" + BOWER_NAME );

  private static final String NPM_NAME = "package.json";
  private static final Pattern NPM_PATTERN =
      Pattern.compile( "META-INF/resources/webjars/([^/]+)/([^/]+)/" + NPM_NAME );

  private static final Pattern PACKAGE_FILES_PATTERN =
      Pattern.compile( "META-INF/resources/webjars/([^/]+)/([^/]+)/.*" );

  private Logger logger = LoggerFactory.getLogger( getClass() );

  public WebjarsURLConnection( URL url ) {
    super( url );
  }

  @Override public void connect() throws IOException {
  }

  @Override public InputStream getInputStream() throws IOException {
    try {

      final PipedOutputStream pipedOutputStream = new PipedOutputStream();
      PipedInputStream pipedInputStream = new PipedInputStream( pipedOutputStream );

      EXECUTOR.submit( new Callable<Void>() {
        @Override public Void call() throws Exception {
          try {
            transform( getURL(), pipedOutputStream );
          } catch ( Exception e ) {
            logger.error( "Error Transforming zip", e );
            pipedOutputStream.close();
          }
          return null;
        }
      } );


      return pipedInputStream;

    } catch ( Exception e ) {
      logger.error( "Error opening Spring xml url", e );
      throw (IOException) new IOException( "Error opening Spring xml url" ).initCause( e );
    }

  }

  private void transform( URL url, PipedOutputStream pipedOutputStream ) throws IOException {
    ArtifactInfo artifactInfo = getArtifactInfo( url );

    URLConnection urlConnection = url.openConnection();
    urlConnection.connect();
    InputStream inputStream = urlConnection.getInputStream();
    JarInputStream jarInputStream = new JarInputStream( inputStream );

    JarOutputStream jarOutputStream = new JarOutputStream( pipedOutputStream, getManifest( artifactInfo, jarInputStream ) );

    ModuleInfo moduleInfo = null;
    Matcher can_fallback_to_files = null;

    ZipEntry entry;
    while ( ( entry = jarInputStream.getNextJarEntry() ) != null ) {
      String name = entry.getName();
      if ( name.endsWith( MANIFEST_MF ) ) {
        // ignore existing manifest, we've already created our own
        continue;
      }

      if ( artifactInfo.group.equals( "org.webjars" ) ) {
        if ( ( moduleInfo == null || !moduleInfo.origin.equals( "pom.xml" ) ) && name.endsWith( POM_NAME ) ) {
          // handcrafted requirejs configuration on pom.xml has top prioriy (Classic WebJars)
          Matcher matcher = POM_PATTERN.matcher( name );
          if ( matcher.matches() ) {
            try {
              moduleInfo = requirejsFromPom( "pom.xml", jarInputStream );

              logger.info( "found pom.xml with requirejs config" );
            } catch ( Exception e ) {
              logger.error( "error reading pom.xml - " + e.getMessage() );
            }

            continue;
          }
        } else if ( name.endsWith( WEBJARS_REQUIREJS_NAME ) ) {
          // next comes the module author's webjars-requirejs.js
          Matcher matcher = MODULE_PATTERN.matcher( name );
          if ( matcher.matches() ) {
            try {
              moduleInfo = requirejsFromJs( "webjars-requirejs.js", matcher, jarInputStream );

              logger.info( "found WEBJARS config" );
            } catch ( Exception e ) {
              logger.error( "error reading WEBJARS config - " + e.getMessage() );
            }

            continue;
          }
        }
      } else if ( moduleInfo == null && artifactInfo.group.equals( "org.webjars.npm" ) && name.endsWith( NPM_NAME ) ) {
        // try to generate requirejs.json from package.json (NPM WebJars)
        Matcher matcher = NPM_PATTERN.matcher( name );
        if ( matcher.matches() ) {
          try {
            moduleInfo = requirejsFromJson( "package.json", matcher, jarInputStream );

            logger.info( "found package.json" );
          } catch ( Exception e ) {
            logger.error( "error reading package.json - " + e.getMessage() );
          }

          continue;
        }
      } else if ( moduleInfo == null && artifactInfo.group.equals( "org.webjars.bower" ) && name.endsWith( BOWER_NAME ) ) {
        // try to generate requirejs.json from bower.json (Bower WebJars)
        Matcher matcher = BOWER_PATTERN.matcher( name );
        if ( matcher.matches() ) {
          try {
            moduleInfo = requirejsFromJson( "bower.json", matcher, jarInputStream );

            logger.info( "found bower.json" );
          } catch ( Exception e ) {
            logger.error( "error reading bower.json - " + e.getMessage() );
          }

          continue;
        }
      }

      jarOutputStream.putNextEntry( entry );
      IOUtils.copy( jarInputStream, jarOutputStream );
      jarOutputStream.closeEntry();

      // store the path of the first file that fits de expected folder structure, for fallback on malformed webjars
      if ( can_fallback_to_files == null ) {
        Matcher matcher = PACKAGE_FILES_PATTERN.matcher( name );

        if ( matcher.matches() ) {
          can_fallback_to_files = matcher;
        }
      }
    }

    if ( moduleInfo == null && can_fallback_to_files != null ) {
      // in last resort generate requirejs config by mapping the root path
      moduleInfo = requirejsFromFileList( "*", can_fallback_to_files );

      logger.info( "built by root path mapping" );
    }

    if ( moduleInfo != null ) {
      try {
        HashMap<String, ?> convertedConfig = getConvertedConfig( artifactInfo, moduleInfo );
        addRequireJsToJar( JSONObject.toJSONString( convertedConfig ), jarOutputStream );

        // Add Blueprint file if we found a require-js configuration.
        ZipEntry newEntry = new ZipEntry( "OSGI-INF/blueprint/blueprint.xml" );
        String blueprintTemplate = IOUtils.toString( getClass().getResourceAsStream( "/org/pentaho/osgi/platform/webjars/blueprint-template.xml" ) );
        blueprintTemplate = blueprintTemplate.replaceAll( "\\{path\\}", "META-INF/resources/webjars/" + moduleInfo.versionedName );
        blueprintTemplate = blueprintTemplate.replace( "{versioned_name}", moduleInfo.versionedName );
        blueprintTemplate = blueprintTemplate.replace( "{name}", moduleInfo.name );
        jarOutputStream.putNextEntry( newEntry );
        jarOutputStream.write( blueprintTemplate.getBytes( "UTF-8" ) );
      } catch ( ParseException e ) {
        logger.error( "error saving " + PENTAHO_RJS_LOCATION + " - " + e.getMessage() );
      }
    }

    // Process webjars into our form
    jarOutputStream.closeEntry();

    pipedOutputStream.flush();
    jarOutputStream.close();
  }

  private ArtifactInfo getArtifactInfo( URL url ) {
    ArtifactInfo artifactInfo;
    String groupId = "unknown";
    String artifactId = "unknown";
    Version version = new Version( 0, 0, 0 );
    if ( url.getProtocol().equals( "file" ) ) {
      String filePath = url.getFile();
      int start = filePath.lastIndexOf( '/' );
      if ( start >= 0 ) {
        artifactId = filePath.substring( filePath.lastIndexOf( '/' ) + 1, filePath.length() );
      } else {
        artifactId = filePath;
      }
    } else if ( url.getProtocol().equals( "mvn" ) ) {
      String[] parts = url.getPath().split( "!", 2 );
      String artifactPart = parts[ parts.length - 1 ];

      parts = artifactPart.split( "/" );
      groupId = parts[ 0 ];
      artifactId = parts[ 1 ];
      String versionPart = parts.length > 2 ? parts[ 2 ] : "LATEST";

      // version needs to be coerced into OSGI form Major.Minor.Patch.Classifier
      version = VersionParser.parseVersion( versionPart );
    }

    artifactInfo = new ArtifactInfo( groupId, artifactId, version.toString() );
    return artifactInfo;
  }

  private HashMap<String, ?> getConvertedConfig( ArtifactInfo artifactInfo, ModuleInfo moduleInfo )
    throws ParseException {
    final HashMap<String, Object> modules = new HashMap<>();
    final HashMap<String, String> artifactModules = new HashMap<>();

    HashMap<String, Object> convertedConfig = modifyConfigPaths( moduleInfo, modules, artifactModules );

    HashMap<String, Object> artifactDetail = new HashMap<>();
    artifactDetail.put( "type", moduleInfo.origin );
    artifactDetail.put( "modules", artifactModules );

    HashMap<String, Object> artifactVersion = new HashMap<>();
    artifactVersion.put( artifactInfo.version, artifactDetail );

    HashMap<String, Object> artifacts = new HashMap<>();
    artifacts.put( artifactInfo.group + "/" + artifactInfo.id, artifactVersion );

    HashMap<String, Object> meta = new HashMap<>();
    meta.put( "modules", modules );
    meta.put( "artifacts", artifacts );

    convertedConfig.put( "requirejs-osgi-meta", meta );
    return convertedConfig;
  }

  private Manifest getManifest( ArtifactInfo artifactInfo, JarInputStream jarInputStream ) {
    Manifest manifest = jarInputStream.getManifest();
    if ( manifest == null ) {
      manifest = new Manifest();
      manifest.getMainAttributes().put( Attributes.Name.MANIFEST_VERSION, "1.0" );
    }
    manifest.getMainAttributes()
        .put( new Attributes.Name( Constants.BUNDLE_SYMBOLICNAME ), "pentaho-webjars-" + artifactInfo.id );
    manifest.getMainAttributes()
        .put( new Attributes.Name( Constants.IMPORT_PACKAGE ),
          "org.osgi.service.http,org.apache.felix.http.api,org.ops4j.pax.web.extender.whiteboard.runtime,"
            + "org.ops4j.pax.web.extender.whiteboard" );

    manifest.getMainAttributes().put( new Attributes.Name( Constants.BUNDLE_VERSION ), artifactInfo.version );
    return manifest;
  }

  private ModuleInfo requirejsFromJs( String origin, Matcher matcher, JarInputStream jarInputStream )
    throws IOException, NoSuchMethodException, ScriptException, ParseException {
    String moduleName = matcher.group( 1 );
    String moduleVersion = matcher.group( 2 );

    byte[] bytes = IOUtils.toByteArray( jarInputStream );

    JSONObject requirejs = convertConfig( new String( bytes, "UTF-8" ) );

    return new ModuleInfo( origin, moduleName, moduleVersion, requirejs, null );
  }

  private JSONObject convertConfig( String config )
    throws ScriptException, NoSuchMethodException, ParseException, IOException {

    Pattern pat = Pattern.compile( "webjars!(.*).js" );
    Matcher m = pat.matcher( config );

    StringBuffer sb = new StringBuffer();
    while ( m.find() ) {
      m.appendReplacement( sb, m.group( 1 ) );
    }
    m.appendTail( sb );

    config = sb.toString();

    pat = Pattern.compile( "webjars\\.path\\(['\"]{1}(.*)['\"]{1}, (['\"]{0,1}[^\\)]+['\"]{0,1})\\)" );
    m = pat.matcher( config );
    while ( m.find() ) {
      m.appendReplacement( sb, m.group( 2 ) );
    }
    m.appendTail( sb );

    config = sb.toString();

    ScriptEngineManager factory = new ScriptEngineManager();
    ScriptEngine engine = factory.getEngineByName( "JavaScript" );
    String script = IOUtils.toString( getClass().getResourceAsStream( "/org/pentaho/osgi/platform/webjars/require-js-aggregator.js" ) );
    script = script.replace( "{{EXTERNAL_CONFIG}}", config );

    //      Context.enter().getWrapFactory().setJavaPrimitiveWrap( false );
    engine.eval( script );

    return (JSONObject) parser.parse( ( (Invocable) engine ).invokeFunction( "processConfig", "" ).toString() );
  }

  private ModuleInfo requirejsFromPom( String origin, JarInputStream jarInputStream )
    throws ParseException, XPathExpressionException, IOException, ParserConfigurationException, SAXException {
    byte[] bytes = IOUtils.toByteArray( jarInputStream );

    Document pom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( new ByteArrayInputStream( bytes ) );

    XPath xPath = XPathFactory.newInstance().newXPath();

    final Element document = pom.getDocumentElement();

    String moduleName = (String) xPath.evaluate( "/project/artifactId", document, XPathConstants.STRING );
    String moduleVersion = (String) xPath.evaluate( "/project/version", document, XPathConstants.STRING );

    HashMap<String, String> dependencies = new HashMap<>();

    String pomConfig = (String) xPath.evaluate( "/project/properties/requirejs", document, XPathConstants.STRING );

    JSONObject requirejs = (JSONObject) parser.parse( pomConfig );

    NodeList pomDependencies = (NodeList) xPath.evaluate( "/project/dependencies/dependency[contains(groupId, 'org.webjars')]", document, XPathConstants.NODESET );
    for ( int i = 0, ic = pomDependencies.getLength(); i != ic; ++i ) {
      Node dependency = pomDependencies.item( i );

      NodeList dependencyChildNodes = dependency.getChildNodes();

      String dependencyGroupId = null;
      String dependencyArtifactId = null;
      String dependencyVersion = null;

      for ( int j = 0, jc = dependencyChildNodes.getLength(); j != jc; ++j ) {
        Node item = dependencyChildNodes.item( j );
        String nodeName = item.getNodeName();

        if ( nodeName.equals( "groupId" ) ) {
          dependencyGroupId = item.getChildNodes().item( 0 ).getNodeValue();
        }

        if ( nodeName.equals( "artifactId" ) ) {
          dependencyArtifactId = item.getChildNodes().item( 0 ).getNodeValue();
        }

        if ( nodeName.equals( "version" ) ) {
          dependencyVersion = item.getChildNodes().item( 0 ).getNodeValue();
        }
      }

      dependencies.put( "pentaho-webjar-deployer:" + dependencyGroupId + "/" + dependencyArtifactId, dependencyVersion );
    }

    return new ModuleInfo( origin, moduleName, moduleVersion, requirejs, dependencies );
  }

  // bower.json and package.json follow very similar format, so it can be parsed by the same method
  private ModuleInfo requirejsFromJson( String origin, Matcher matcher, JarInputStream jarInputStream ) throws ParseException,
    IOException {
    String moduleName = matcher.group( 1 );
    String moduleVersion = matcher.group( 2 );

    HashMap<String, String> dependencies = new HashMap<>();

    byte[] bytes = IOUtils.toByteArray( jarInputStream );

    String packageConfig = new String( bytes, "UTF-8" );

    JSONObject json = (JSONObject) parser.parse( packageConfig );

    HashMap<String, String> paths = new HashMap<>();
    paths.put( moduleName, "" );

    HashMap<String, String> map = new HashMap<>();

    HashMap<String, String> pck = extractPackage( json, moduleName, paths, map );

    JSONObject requirejs = new JSONObject();

    if ( !map.isEmpty() ) {
      HashMap<String, HashMap<String, String>> topmap = new HashMap<>();
      topmap.put( moduleName, map );

      requirejs.put( "map", topmap );
    }

    if ( json.containsKey( "dependencies" ) ) {
      HashMap<String, ?> deps = (HashMap<String, ?>) json.get( "dependencies" );
      final Set<String> depsKeySet = deps.keySet();

      HashMap<String, Object> shim = new HashMap<>();

      for ( String key : paths.keySet() ) {
        HashMap<String, ArrayList<String>> shim_deps = new HashMap<>();
        shim_deps.put( "deps", new ArrayList<>( depsKeySet ) );

        shim.put( key, shim_deps );
      }

      if ( pck != null ) {
        HashMap<String, ArrayList<String>> shim_deps = new HashMap<>();
        shim_deps.put( "deps", new ArrayList<>( depsKeySet ) );

        shim.put(  pck.get( "name" ) + "/" + moduleVersion + "/" + pck.get( "main" ), shim_deps );
      }

      requirejs.put( "shim", shim );

      for ( String key : depsKeySet ) {
        dependencies.put( key, (String) deps.get( key ) );
      }
    }

    requirejs.put( "paths", paths );
    if ( pck != null ) {
      ArrayList<HashMap<String, String>> packages = new ArrayList<>();
      packages.add( pck );
      requirejs.put( "packages", packages );
    }

    return new ModuleInfo( origin, moduleName, moduleVersion, requirejs, dependencies );
  }

  private ModuleInfo requirejsFromFileList( String origin, Matcher matcher ) {
    String moduleName = matcher.group( 1 );
    String moduleVersion = matcher.group( 2 );

    HashMap<String, String> paths = new HashMap<>();
    paths.put( moduleName, "" );

    JSONObject requirejs = new JSONObject();
    requirejs.put( "paths", paths );

    return new ModuleInfo( origin, moduleName, moduleVersion, requirejs, null );
  }

  private HashMap<String, String> extractPackage( JSONObject json, String moduleName, HashMap<String, String> paths,
                                                  HashMap<String, String> map ) {
    HashMap<String, String> pck = null;
    if ( json.containsKey( "main" ) ) {
      // npm: https://docs.npmjs.com/files/package.json#main
      // bower: https://github.com/bower/spec/blob/master/json.md#main
      Object value = json.get( "main" );

      if ( value instanceof String ) {
        pck = packageFromFilename( (String) value, moduleName );
      } else if ( value instanceof JSONArray ) {
        JSONArray files = (JSONArray) value;

        for ( Object file : files ) {
          final HashMap<String, String> pack = packageFromFilename( (String) file, moduleName );
          if ( pack != null ) {
            pck = pack;
            break;
          }
        }
      }
    }

    if ( json.containsKey( "browser" ) ) {
      // "browser" field for package.json: https://gist.github.com/defunctzombie/4339901
      Object value = json.get( "browser" );

      if ( value instanceof String ) {
        // alternate main - basic
        pck = packageFromFilename( (String) value, moduleName );
      } else if ( value instanceof HashMap ) {
        // replace specific files - advanced
        HashMap<String, ?> overridePaths = (HashMap<String, ?>) value;

        for ( String overridePath : overridePaths.keySet() ) {
          Object replaceRawValue = overridePaths.get( overridePath );

          String replaceValue;
          if ( replaceRawValue instanceof String ) {
            replaceValue = (String) replaceRawValue;
            if ( replaceValue.startsWith( "./" ) ) {
              replaceValue = replaceValue.substring( 2 );
            }
            replaceValue = FilenameUtils.removeExtension( replaceValue );
          } else {
            // ignore a module
            // TODO: Should redirect to an empty module
            replaceValue = "no-where-to-be-found";
          }

          if ( overridePath.startsWith( "./" ) ) {
            paths.put( FilenameUtils.removeExtension( overridePath ), replaceValue  );
          } else {
            map.put( FilenameUtils.removeExtension( overridePath ), replaceValue );
          }
        }
      }
    }

    return pck;
  }

  private HashMap<String, String> packageFromFilename( String file, String moduleName ) {
    if ( FilenameUtils.getExtension( file ).equals( "js" ) ) {
      String filename = FilenameUtils.removeExtension( file );

      HashMap<String, String> pck = new HashMap<>();
      pck.put( "name", moduleName );
      pck.put( "main", filename );

      return pck;
    }

    return null;
  }

  private HashMap<String, Object> modifyConfigPaths( ModuleInfo moduleInfo, HashMap<String, Object> modules,
                                                     HashMap<String, String> artifactModules ) throws ParseException {
    HashMap<String, Object> requirejs = new HashMap<>();

    HashMap<String, String> keyMap = new HashMap<>();

    HashMap<String, String> paths = (HashMap<String, String>) moduleInfo.requirejs.get( "paths" );
    if ( paths != null ) {
      HashMap<String, String> convertedPaths = new HashMap<>();

      for ( String key : paths.keySet() ) {
        String versionedKey;
        if ( key.startsWith( "./" ) ) {
          versionedKey = moduleInfo.name + "/" + moduleInfo.version + key.substring( 1 );
        } else {
          versionedKey = key + "/" + moduleInfo.version;

          HashMap<String, Object> moduleDetails = new HashMap<>();
          if ( moduleInfo.dependencies != null && !moduleInfo.dependencies.isEmpty() ) {
            moduleDetails.put( "dependencies", moduleInfo.dependencies );
          }

          HashMap<String, Object> module = new HashMap<>();
          module.put( moduleInfo.version, moduleDetails );

          modules.put( key, module );

          artifactModules.put( key, moduleInfo.version );
        }

        keyMap.put( key, versionedKey );

        String path = paths.get( key );
        if ( path.length() > 0 && !path.startsWith( "/" ) ) {
          path = "/" + path;
        }

        convertedPaths.put( versionedKey, moduleInfo.versionedName + path );
      }

      requirejs.put( "paths", convertedPaths );
    }

    ArrayList<HashMap<String, String>> packages = (ArrayList<HashMap<String, String>>) moduleInfo.requirejs.get( "packages" );
    if ( packages != null ) {
      ArrayList<HashMap<String, String>> convertedPackages = new ArrayList<>();

      for ( HashMap<String, String> pck : packages ) {
        if ( pck.containsKey( "name" ) ) {
          pck.put( "name", pck.get( "name" ) + "/" + moduleInfo.version );
        }

        convertedPackages.add( pck );
      }

      requirejs.put( "packages", convertedPackages );
    }

    requirejs.put( "shim", converteSubConfig( keyMap, (HashMap<String, ?>) moduleInfo.requirejs.get( "shim" ) ) );

    requirejs.put( "map", converteSubConfig( keyMap, (HashMap<String, ?>) moduleInfo.requirejs.get( "map" ) ) );

    return requirejs;
  }

  private HashMap<String, ?> converteSubConfig( HashMap<String, String> keyMap,
                                                     HashMap<String, ?> subConfig ) {
    HashMap<String, Object> convertedSubConfig = new HashMap<>();

    if ( subConfig != null ) {
      for ( String key : subConfig.keySet() ) {
        String versionedKey = keyMap.get( key );

        if ( versionedKey != null ) {
          convertedSubConfig.put( versionedKey, subConfig.get( key ) );
        } else {
          convertedSubConfig.put( key, subConfig.get( key ) );
        }
      }
    }

    return convertedSubConfig;
  }

  private void addRequireJsToJar( String config, JarOutputStream jarOutputStream ) throws IOException {
    ZipEntry newEntry = new ZipEntry( PENTAHO_RJS_LOCATION );
    jarOutputStream.putNextEntry( newEntry );
    jarOutputStream.write( config.getBytes( "UTF-8" ) );
    jarOutputStream.closeEntry();
  }

  private class ArtifactInfo {
    String group;
    String id;
    String version;

    ArtifactInfo( String group, String id, String version ) {
      this.group = group;
      this.id = id;
      this.version = version;
    }
  }

  private class ModuleInfo {
    String origin;

    String name;
    String version;

    String versionedName;

    JSONObject requirejs;
    HashMap<String, String> dependencies;

    ModuleInfo( String origin, String moduleName, String moduleVersion, JSONObject requirejs, HashMap<String, String> dependencies ) {
      this.origin = origin;

      this.name = moduleName;
      this.version = moduleVersion;

      this.versionedName = this.name + "/" + this.version;

      this.requirejs = requirejs;
      this.dependencies = dependencies;
    }
  }
}
