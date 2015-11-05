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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import java.io.File;
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
import java.util.Iterator;
import java.util.ArrayList;

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

  public static final String MANIFEST_MF = "MANIFEST.MF";
  public static final String PENTAHO_RJS_LOCATION = "META-INF/js/require.json";

  public static final String WEBJARS_REQUIREJS_NAME = "webjars-requirejs.js";
  public static final Pattern MODULE_PATTERN =
      Pattern.compile( "META-INF/resources/webjars/([^/]+)/([^/]+)/" + WEBJARS_REQUIREJS_NAME );

  public static final String POM_NAME = "pom.xml";
  public static final Pattern POM_PATTERN =
      Pattern.compile( "META-INF/maven/org.webjars([^/]*)/([^/]+)/" + POM_NAME );

  public static final String BOWER_NAME = "bower.json";
  public static final Pattern BOWER_PATTERN =
      Pattern.compile( "META-INF/resources/webjars/([^/]+)/([^/]+)/" + BOWER_NAME );

  public static final String NPM_NAME = "package.json";
  public static final Pattern NPM_PATTERN =
      Pattern.compile( "META-INF/resources/webjars/([^/]+)/([^/]+)/" + NPM_NAME );

  public static final Pattern JS_PATTERN =
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

    String artifactName = "unknown";
    Version version = new Version( 0, 0, 0 );
    if ( url.getProtocol().equals( "file" ) ) {
      String filePath = url.getFile();
      int start = filePath.lastIndexOf( '/' );
      if ( start >= 0 ) {
        artifactName = filePath.substring( filePath.lastIndexOf( '/' ) + 1, filePath.length() );  
      } else {
        artifactName = filePath;
      }
    } else if ( url.getProtocol().equals( "mvn" ) ) {
      String[] parts = url.getPath().split( "/" );
      artifactName = parts[ 1 ];
      String versionPart = parts[ 2 ];

      // version needs to be coerced into OSGI form Major.Minor.Patch.Classifier
      version = VersionParser.parseVersion( versionPart );
    }

    URLConnection urlConnection = url.openConnection();
    urlConnection.connect();
    InputStream inputStream = urlConnection.getInputStream();
    JarInputStream jarInputStream = new JarInputStream( inputStream );

    Manifest manifest = jarInputStream.getManifest();
    if ( manifest == null ) {
      manifest = new Manifest();
      manifest.getMainAttributes().put( Attributes.Name.MANIFEST_VERSION, "1.0" );
    }
    manifest.getMainAttributes()
        .put( new Attributes.Name( Constants.BUNDLE_SYMBOLICNAME ), "pentaho-webjars-" + artifactName );
    manifest.getMainAttributes()
        .put( new Attributes.Name( Constants.IMPORT_PACKAGE ),
          "org.osgi.service.http,org.apache.felix.http.api,org.ops4j.pax.web.extender.whiteboard.runtime,"
            + "org.ops4j.pax.web.extender.whiteboard" );

    manifest.getMainAttributes().put( new Attributes.Name( Constants.BUNDLE_VERSION ), version.toString() );

    JarOutputStream jarOutputStream = new JarOutputStream( pipedOutputStream, manifest );

    ZipEntry entry;
    String moduleName = "unknown";
    String moduleVersion = "unknown";
    
    int foundRJs = Integer.MAX_VALUE;
    ArrayList<String> js_files = new ArrayList<>();

    while ( ( entry = jarInputStream.getNextJarEntry() ) != null ) {
      String name = entry.getName();
      if ( name.endsWith( MANIFEST_MF ) ) {
        // ignore existing manifest, we'll update it after the copy
        logger.info( "skipping manifest" );

        continue;
      }

      if ( name.endsWith( WEBJARS_REQUIREJS_NAME ) ) {

        // webjars-requirejs.js has top prioriy
        if(foundRJs < 1) {
          continue;
        }

        Matcher matcher = MODULE_PATTERN.matcher( name );
        if ( matcher.matches() == false ) {
          logger.error( "Webjars structure isn't right" );
          continue;
        }
        
        foundRJs = 0;

        logger.info( "found WEBJARS config" );
        moduleName = matcher.group( 1 );
        moduleVersion = matcher.group( 2 );

        byte[] bytes = IOUtils.toByteArray( jarInputStream );

        String webjarsConfig = new String( bytes, "UTF-8" );

        String convertedConfig = convertConfig( webjarsConfig, moduleName, moduleVersion );

        addRequireJsToJar(convertedConfig, jarOutputStream);

      } else if ( name.endsWith( POM_NAME ) ) {

        // next comes the requirejs configuration on pom.xml (Classic WebJars)
        if(foundRJs < 2) {
          continue;
        }

        Matcher matcher = POM_PATTERN.matcher( name );
        if ( !matcher.matches() ) {
          logger.error( "pom.xml location isn't right" );
          continue;
        }

        try {
          byte[] bytes = IOUtils.toByteArray( jarInputStream );
          Document pom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( new ByteArrayInputStream( bytes ) );

          XPath xPath = XPathFactory.newInstance().newXPath();
          
          String pomConfig = ( String ) xPath.evaluate("/project/properties/requirejs", pom.getDocumentElement(), XPathConstants.STRING);

          moduleName = (String) xPath.evaluate( "/project/artifactId", pom.getDocumentElement(), XPathConstants.STRING );
          moduleVersion = (String) xPath.evaluate( "/project/version", pom.getDocumentElement(), XPathConstants.STRING );

          String convertedConfig = modifyConfigPaths( pomConfig, moduleName, moduleVersion );

          addRequireJsToJar(convertedConfig, jarOutputStream);
          
          foundRJs = 1;
          logger.info( "found pom.xml with requirejs config" );

        } catch ( Exception e ) {
          logger.error( "error reading pom.xml - " + e.getMessage() );
        }

      } else if ( name.endsWith( BOWER_NAME ) ) {
        // next try to generate requirejs.json from bower.json (Bower WebJars)
        if ( foundRJs < 3 ) {
          continue;
        }

        Matcher matcher = BOWER_PATTERN.matcher( name );
        if ( !matcher.matches() ) {
          logger.error( "bower.json location isn't right" );
          continue;
        }

        moduleName = matcher.group( 1 );
        moduleVersion = matcher.group( 2 );

        byte[] bytes = IOUtils.toByteArray( jarInputStream );

        String bowerConfig = new String( bytes, "UTF-8" );

        try {
          requirejs = requirejsFromJson( bowerConfig, moduleName, moduleVersion, dependencies );

          foundRJs = 3;
          logger.info( "found bower.json" );
          origin = "bower.json";
        } catch ( Exception e ) {
          logger.error( "error reading bower.json - " + e.getMessage() );
        }
      } else if ( name.endsWith( NPM_NAME ) ) {
        // next try to generate requirejs.json from package.json (NPM WebJars)
        if ( foundRJs < 4 ) {
          continue;
        }

        Matcher matcher = NPM_PATTERN.matcher( name );
        if ( !matcher.matches() ) {
          logger.error( "package.json location isn't right" );
          continue;
        }

        moduleName = matcher.group( 1 );
        moduleVersion = matcher.group( 2 );

        byte[] bytes = IOUtils.toByteArray( jarInputStream );

        String packageConfig = new String( bytes, "UTF-8" );

        try {

          String convertedConfig = requirejsFromJson( packageConfig, moduleName, moduleVersion );

          addRequireJsToJar(convertedConfig, jarOutputStream);
          
          foundRJs = 4;
          logger.info( "found package.json" );

        } catch ( Exception e ) {
          logger.error( "error reading package.json - " + e.getMessage() );
        }
      } else {
        jarOutputStream.putNextEntry( entry );
        IOUtils.copy( jarInputStream, jarOutputStream );
        jarOutputStream.closeEntry();

        // store JS filenames for require.json fallback generation on malformed webjars
        if ( foundRJs == Integer.MAX_VALUE && FilenameUtils.getExtension( name ).equals( "js" ) ) {
          js_files.add( name );
        }
      }
    }

    int js_count = js_files.size();
    if( foundRJs == Integer.MAX_VALUE && js_count > 0 ) {
      Iterator<String> iterator = js_files.iterator();
      
      JSONObject paths = new JSONObject();

      while ( iterator.hasNext() ) {
        String file = iterator.next();

        Matcher matcher = JS_PATTERN.matcher( file );
        if ( !matcher.matches() ) {
          continue;
        }

        moduleName = matcher.group( 1 );
        moduleVersion = matcher.group( 2 );

        String filename = FilenameUtils.getBaseName( file );

        if( js_count == 1 || filename.equals(moduleName) ) {
          paths.put( moduleName, moduleName + "/" + moduleVersion + "/" + filename );
        } else {
          paths.put( moduleName, moduleName + "/" + moduleVersion );
          paths.put( moduleName + "/" + filename, moduleName + "/" + moduleVersion + "/" + filename );
        }
      }

      JSONObject requirejs = new JSONObject();
      requirejs.put( "paths", paths );

      addRequireJsToJar(requirejs.toJSONString(), jarOutputStream);
      
      foundRJs = 5;
      logger.info( "built from file list" );
    }

    // Add Blueprint file if we found a require-js configuration.
    if( foundRJs != Integer.MAX_VALUE ) {
      ZipEntry newEntry = new ZipEntry( "OSGI-INF/blueprint/blueprint.xml" );
      String blueprintTemplate = IOUtils.toString( getClass().getResourceAsStream(
          "/org/pentaho/osgi/platform/webjars/blueprint-template.xml" ) );
      blueprintTemplate = blueprintTemplate.replaceAll( "\\{path\\}",
          "META-INF/resources/webjars/" + moduleName + "/" + moduleVersion );
      blueprintTemplate = blueprintTemplate.replace( "{versioned_name}", moduleName + "/" + moduleVersion );
      blueprintTemplate = blueprintTemplate.replace( "{name}", moduleName );
      jarOutputStream.putNextEntry( newEntry );
      jarOutputStream.write( blueprintTemplate.getBytes( "UTF-8" ) );
    }

    // Process webjars into our form
    jarOutputStream.closeEntry();

    pipedOutputStream.flush();
    jarOutputStream.close();
  }

  private String convertConfig( String config, String moduleName, String moduleVersion ) {

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

  private JSONObject requirejsFromPom( Element document, String moduleName, String moduleVersion,
                                        HashMap<String, String> dependencies )
    throws ParseException, XPathExpressionException {
    XPath xPath = XPathFactory.newInstance().newXPath();

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

    return requirejs;
  }

  // bower.json and package.json follow very similar format, so it can be parsed by the same method
  private JSONObject requirejsFromJson( String config, String moduleName, String moduleVersion,
                                        HashMap<String, String> dependencies ) throws ParseException {
    JSONObject json = (JSONObject) parser.parse( config );

    HashMap<String, String> paths = new HashMap<>();

    extractPaths( json, "files", moduleName, paths );
    extractPaths( json, "main", moduleName, paths );

    HashMap<String, Object> shim = new HashMap<>();

    if ( json.containsKey( "dependencies" ) ) {
      HashMap<String, Object> deps = (HashMap<String, Object>) json.get( "dependencies" );
      final Set<String> depsKeySet = deps.keySet();

      for ( String key : paths.keySet() ) {
        HashMap<String, ArrayList<String>> shim_deps = new HashMap<>();
        shim_deps.put( "deps", new ArrayList<>( depsKeySet ) );

        shim.put( key, shim_deps );
      }

      for ( String key : depsKeySet ) {
        dependencies.put( key, (String) deps.get( key ) );
      }
    }

    JSONObject requirejs = new JSONObject();
    requirejs.put( "paths", paths );
    requirejs.put( "shim", shim );

    return requirejs;
  }

  private void extractPaths( JSONObject json, String jsonKey, String moduleName, HashMap<String, String> paths ) {
    if ( json.containsKey( jsonKey ) ) {
      Object value = json.get( jsonKey );

      if( value instanceof String ) {
        pathFromFilename( (String) value, moduleName, paths );
      } else if ( value instanceof JSONArray ) {
        JSONArray files = (JSONArray) value;

        for ( Object file : files ) {
          pathFromFilename( (String) file, moduleName, paths );
        }
      }
    }
  }

  private void pathFromFilename( String file, String moduleName, HashMap<String, String> paths ) {
    if ( FilenameUtils.getExtension( file ).equals( "js" ) ) {
      String filename = FilenameUtils.removeExtension( file );
      String key = filename.equals( moduleName ) ? "" : filename;

      paths.put( key, filename );
    } else {
      paths.put( file, file );
    }
  }

  private HashMap<String, HashMap<String, ?>> modifyConfigPaths( JSONObject config, boolean configGenerated, String moduleName,
                                                     String moduleVersion ) throws ParseException {
    HashMap<String, HashMap<String, ?>> requirejs = new HashMap<>();

    HashMap<String, String> keyMap = new HashMap<>();

    HashMap<String, String> convertedPaths = new HashMap<>();
    HashMap<String, Object> convertedShim = new HashMap<>();
    HashMap<String, Object> convertedMap = new HashMap<>();

    HashMap<String, Object> paths = (HashMap<String, Object>) config.get( "paths" );
    if ( paths != null ) {
      for ( String key : paths.keySet() ) {
        String versionedKey;
        if( configGenerated ) {
          versionedKey = moduleName + "/" + key + "/" + moduleVersion;
        } else {
          versionedKey = key + "/" + moduleVersion;
        }

        keyMap.put( key, versionedKey );

        convertedPaths.put( versionedKey, moduleName + "/" + moduleVersion + "/" + paths.get( key ) );
      }

      requirejs.put( "paths", convertedPaths );
    }

    HashMap<String, Object> shim = (HashMap<String, Object>) config.get( "shim" );
    if ( shim != null ) {
      for ( String key : shim.keySet() ) {
        String versionedKey = keyMap.get( key );

        if ( versionedKey != null ) {
          convertedShim.put( versionedKey, shim.get( key ) );
        } else {
          convertedShim.put( key, shim.get( key ) );
        }
      }

      requirejs.put( "shim", convertedShim );
    }

    HashMap<String, Object> map = (HashMap<String, Object>) config.get( "map" );
    if ( map != null ) {
      for ( String key : map.keySet() ) {
        String versionedKey = keyMap.get( key );

        if ( versionedKey != null ) {
          convertedMap.put( versionedKey, map.get( key ) );
        } else {
          convertedMap.put( key, map.get( key ) );
        }
      }
    }

    requirejs.put( "map", convertedMap );

    return requirejs;
  }

  private void addRequireJsToJar( String config, JarOutputStream jarOutputStream ) throws IOException {
    ZipEntry newEntry = new ZipEntry( PENTAHO_RJS_LOCATION );
    jarOutputStream.putNextEntry( newEntry );
    jarOutputStream.write( config.getBytes( "UTF-8" ) );
    jarOutputStream.closeEntry();
  }
}
