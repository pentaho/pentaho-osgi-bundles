package org.pentaho.js.require;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nantunes on 12/11/15.
 */
public class RequireJsGenerator {
  private final JSONParser parser = new JSONParser();

  private ModuleInfo moduleInfo;

  private JSONObject requireConfig;
  private HashMap<String, String> dependencies = new HashMap<>();

  public RequireJsGenerator( Document pom ) throws XPathExpressionException, ParseException {
    requirejsFromPom( pom );
  }

  public RequireJsGenerator( String moduleName, String moduleVersion, String jsScript )
      throws NoSuchMethodException, ScriptException, ParseException, IOException {
    requirejsFromJs( moduleName, moduleVersion, jsScript );
  }

  public RequireJsGenerator( JSONObject json ) {
    requirejsFromJson( json );
  }

  public RequireJsGenerator( String moduleName, String moduleVersion ) {
    moduleInfo = new ModuleInfo( moduleName, moduleVersion );

    HashMap<String, String> paths = new HashMap<>();
    paths.put( moduleName, "" );

    requireConfig = new JSONObject();
    requireConfig.put( "paths", paths );
  }

  public ModuleInfo getConvertedConfig( ArtifactInfo artifactInfo ) throws ParseException {
    return this.getConvertedConfig( artifactInfo, true, null );
  }

  public ModuleInfo getConvertedConfig( ArtifactInfo artifactInfo, boolean isAmdPackage, String exports )
      throws ParseException {
    moduleInfo.setAmdPackage( isAmdPackage );
    moduleInfo.setExports( exports );

    final HashMap<String, String> artifactModules = new HashMap<>();

    JSONObject convertedConfig = modifyConfigPaths( artifactModules );

    HashMap<String, Object> artifactVersion = new HashMap<>();
    artifactVersion.put( artifactInfo.getVersion(), artifactModules );

    HashMap<String, Object> artifacts = new HashMap<>();
    artifacts.put( artifactInfo.getGroup() + "/" + artifactInfo.getArtifactId(), artifactVersion );

    HashMap<String, Object> meta = new HashMap<>();
    meta.put( "modules", moduleInfo.getModules() );
    meta.put( "artifacts", artifacts );

    convertedConfig.put( "requirejs-osgi-meta", meta );

    moduleInfo.setRequireJs( convertedConfig );

    return moduleInfo;
  }

  private void requirejsFromPom( Document pom )
      throws XPathExpressionException, ParseException {
    XPath xPath = XPathFactory.newInstance().newXPath();

    final Element document = pom.getDocumentElement();

    moduleInfo = new ModuleInfo( (String) xPath.evaluate( "/project/artifactId", document, XPathConstants.STRING ),
        (String) xPath.evaluate( "/project/version", document, XPathConstants.STRING ) );

    String pomConfig = (String) xPath.evaluate( "/project/properties/requirejs", document, XPathConstants.STRING );

    requireConfig = (JSONObject) parser.parse( pomConfig );

    NodeList pomDependencies = (NodeList) xPath
        .evaluate( "/project/dependencies/dependency[contains(groupId, 'org.webjars')]", document,
            XPathConstants.NODESET );
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

      dependencies
          .put( "pentaho-webjar-deployer:" + dependencyGroupId + "/" + dependencyArtifactId, dependencyVersion );
    }
  }

  private void requirejsFromJs( String moduleName, String moduleVersion, String jsScript )
      throws IOException, ScriptException, NoSuchMethodException, ParseException {
    moduleInfo = new ModuleInfo( moduleName, moduleVersion );

    Pattern pat = Pattern.compile( "webjars!(.*).js" );
    Matcher m = pat.matcher( jsScript );

    StringBuffer sb = new StringBuffer();
    while ( m.find() ) {
      m.appendReplacement( sb, m.group( 1 ) );
    }
    m.appendTail( sb );

    jsScript = sb.toString();

    pat = Pattern.compile( "webjars\\.path\\(['\"]{1}(.*)['\"]{1}, (['\"]{0,1}[^\\)]+['\"]{0,1})\\)" );
    m = pat.matcher( jsScript );
    while ( m.find() ) {
      m.appendReplacement( sb, m.group( 2 ) );
    }
    m.appendTail( sb );

    jsScript = sb.toString();

    ScriptEngineManager factory = new ScriptEngineManager();
    ScriptEngine engine = factory.getEngineByName( "JavaScript" );
    String script = IOUtils
        .toString( getClass().getResourceAsStream( "/org/pentaho/js/require/require-js-aggregator.js" ) );
    script = script.replace( "{{EXTERNAL_CONFIG}}", jsScript );

    engine.eval( script );

    requireConfig =
        (JSONObject) parser.parse( ( (Invocable) engine ).invokeFunction( "processConfig", "" ).toString() );
  }

  // bower.json and package.json follow very similar format, so it can be parsed by the same method
  private void requirejsFromJson( JSONObject json ) {
    moduleInfo = new ModuleInfo( (String) json.get( "name" ), (String) json.get( "version" ) );

    if ( json.containsKey( "path" ) ) {
      moduleInfo.setPath( (String) json.get( "path" ) );
    }

    JSONObject paths = json.containsKey( "paths" ) ? (JSONObject) json.get( "paths" ) : new JSONObject();
    paths.put( moduleInfo.getName(), moduleInfo.getPath() );

    JSONObject map = json.containsKey( "map" ) ? (JSONObject) json.get( "map" ) : new JSONObject();

    Object pck = extractPackage( json, moduleInfo.getName(), paths, map );

    requireConfig = new JSONObject();

    if ( !map.isEmpty() ) {
      HashMap<String, HashMap<String, String>> topmap = new HashMap<>();
      topmap.put( moduleInfo.getName(), map );

      requireConfig.put( "map", topmap );
    }

    if ( json.containsKey( "dependencies" ) ) {
      HashMap<String, ?> deps = (HashMap<String, ?>) json.get( "dependencies" );

      final Set<String> depsKeySet = deps.keySet();
      for ( String key : depsKeySet ) {
        dependencies.put( key, (String) deps.get( key ) );
      }
    }

    requireConfig.put( "paths", paths );

    JSONArray packages = json.containsKey( "packages" ) ? (JSONArray) json.get( "packages" ) : new JSONArray();
    if ( pck != null ) {
      packages.add( pck );
    }

    requireConfig.put( "packages", packages );
  }

  private Object extractPackage( JSONObject json, String moduleName, HashMap<String, String> paths,
                                 HashMap<String, String> map ) {
    Object pck = null;
    if ( json.containsKey( "main" ) ) {
      // npm: https://docs.npmjs.com/files/package.json#main
      // bower: https://github.com/bower/spec/blob/master/json.md#main
      Object value = json.get( "main" );

      if ( value instanceof String ) {
        pck = packageFromFilename( (String) value );
      } else if ( value instanceof JSONArray ) {
        JSONArray files = (JSONArray) value;

        for ( Object file : files ) {
          final Object pack = packageFromFilename( (String) file );
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
        pck = packageFromFilename( (String) value );
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
            paths.put( FilenameUtils.removeExtension( overridePath ), replaceValue );
          } else {
            map.put( FilenameUtils.removeExtension( overridePath ), replaceValue );
          }
        }
      }
    }

    return pck;
  }

  private Object packageFromFilename( String file ) {
    if ( file.startsWith( "./" ) ) {
      file = file.substring( 2 );
    } else if ( file.startsWith( "/" ) ) {
      file = file.substring( 1 );
    }

    if ( FilenameUtils.getExtension( file ).equals( "js" ) ) {
      if ( file.equals( "main.js" ) ) {
        return "";
      }

      String filename = FilenameUtils.removeExtension( file );

      HashMap<String, String> pck = new HashMap<>();
      pck.put( "name", "" );
      if ( !moduleInfo.getPath().isEmpty() ) {
        pck.put( "location", moduleInfo.getPath() );
      }
      pck.put( "main", filename );

      return pck;
    }

    return null;
  }

  private JSONObject modifyConfigPaths( HashMap<String, String> artifactModules ) throws ParseException {
    JSONObject requirejs = new JSONObject();

    HashMap<String, String> keyMap = new HashMap<>();

    HashMap<String, Object> moduleDetails = new HashMap<>();
    if ( dependencies != null && !dependencies.isEmpty() ) {
      moduleDetails.put( "dependencies", dependencies );
    }

    final boolean isAmdPackage = moduleInfo.isAmdPackage();
    moduleDetails.put( "isAmdPackage", isAmdPackage );
    if ( !isAmdPackage && moduleInfo.getExports() != null ) {
      moduleDetails.put( "exports", moduleInfo.getExports() );
    }

    final HashMap<String, String> paths = (HashMap<String, String>) requireConfig.get( "paths" );
    if ( paths != null ) {
      HashMap<String, String> convertedPaths = new HashMap<>();

      for ( String key : paths.keySet() ) {
        String versionedKey;
        if ( key.startsWith( "./" ) ) {
          versionedKey = moduleInfo.getVersionedName() + key.substring( 1 );
        } else {
          versionedKey = key + "_" + moduleInfo.getVersion();

          HashMap<String, Object> module = new HashMap<>();
          module.put( moduleInfo.getVersion(), moduleDetails );

          moduleInfo.addModuleId( key, module );

          artifactModules.put( key, moduleInfo.getVersion() );
        }

        keyMap.put( key, versionedKey );

        String path = paths.get( key );
        if ( path.length() > 0 ) {
          if ( path.startsWith( "/" ) ) {
            convertedPaths.put( versionedKey, path );
          } else {
            convertedPaths.put( versionedKey, moduleInfo.getVersionedPath() + "/" + path );
          }
        } else {
          convertedPaths.put( versionedKey, moduleInfo.getVersionedPath() );
        }
      }

      requirejs.put( "paths", convertedPaths );
    }

    final JSONArray packages = (JSONArray) requireConfig.get( "packages" );
    if ( packages != null && !packages.isEmpty() ) {
      moduleDetails.put( "packages", SerializationUtils.clone( packages ) );

      JSONArray convertedPackages = new JSONArray();

      for ( Object pack : packages ) {
        if ( pack instanceof String ) {
          String packageName = (String) pack;

          String convertedName;
          if ( !packageName.isEmpty() ) {
            convertedName = moduleInfo.getVersionedName() + "/" + packageName;
          } else {
            packageName = moduleInfo.getName();
            convertedName = moduleInfo.getVersionedName();
          }

          keyMap.put( packageName, convertedName );
          keyMap.put( packageName + "/main", convertedName + "/main" );

          convertedPackages.add( convertedName );
        } else if ( pack instanceof HashMap ) {
          final HashMap<String, String> packageObj = (HashMap<String, String>) pack;

          if ( ( (HashMap) pack ).containsKey( "name" ) ) {
            String packageName = packageObj.get( "name" );
            final String mainScript = ( (HashMap) pack ).containsKey( "main" ) ? packageObj.get( "main" ) : "main";

            String convertedName;
            if ( !packageName.isEmpty() ) {
              convertedName = moduleInfo.getVersionedName() + "/" + packageName;
            } else {
              packageName = moduleInfo.getName();
              convertedName = moduleInfo.getVersionedName();
            }

            keyMap.put( packageName, convertedName );
            keyMap.put( packageName + "/" + mainScript, convertedName + "/" + mainScript );

            packageObj.put( "name", convertedName );
          }

          convertedPackages.add( pack );
        }
      }

      requirejs.put( "packages", convertedPackages );
    }

    final HashMap<String, ?> shim = (HashMap<String, ?>) requireConfig.get( "shim" );
    if ( shim != null ) {
      requirejs.put( "shim", convertSubConfig( keyMap, shim ) );
    }

    final HashMap<String, ?> map = (HashMap<String, ?>) requireConfig.get( "map" );
    if ( map != null ) {
      requirejs.put( "map", convertSubConfig( keyMap, map ) );
    }

    if ( requireConfig.containsKey( "config" ) ) {
      requirejs.put( "config", requireConfig.get( "config" ) );
    }

    return requirejs;
  }

  private HashMap<String, ?> convertSubConfig( HashMap<String, String> keyMap,
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

  public static class ModuleInfo {
    private String name;
    private String version;
    private String path;

    private boolean isAmdPackage;
    private String exports;

    private String versionedModuleId;
    private String versionedPath;

    final HashMap<String, Object> modules;

    private JSONObject requireJs;

    public ModuleInfo( String moduleName, String moduleVersion ) {
      this.name = moduleName;
      this.version = moduleVersion;
      this.isAmdPackage = true;
      this.exports = null;

      this.versionedModuleId = this.name + "_" + this.version;
      this.versionedPath = this.name + "/" + this.version;

      this.modules = new HashMap<>();
    }

    public String getName() {
      return name;
    }

    public void setName( String name ) {
      this.name = name;
    }

    public String getVersion() {
      return version;
    }

    public void setVersion( String version ) {
      this.version = version;
    }

    public String getPath() {
      return this.path != null ? this.path : "";
    }

    public void setPath( String path ) {
      this.path = path;
    }

    public boolean isAmdPackage() {
      return isAmdPackage;
    }

    public void setAmdPackage( boolean amdPackage ) {
      isAmdPackage = amdPackage;
    }

    public String getExports() {
      return this.exports;
    }

    public void setExports( String exports ) {
      this.exports = exports;
    }

    public void addModuleId( String key, Object module ) {
      this.modules.put( key, module );
    }

    public HashMap<String, Object> getModules() {
      return this.modules;
    }

    public String getVersionedName() {
      return versionedModuleId;
    }

    public String getVersionedPath() {
      return versionedPath;
    }

    public JSONObject getRequireJs() {
      return requireJs;
    }

    public void setRequireJs( JSONObject requireJs ) {
      this.requireJs = requireJs;
    }
  }

  public static class ArtifactInfo {
    private String group = "unknown";
    private String artifactId = "unknown";
    private String version = "0.0.0";
    private String osgiCompatibleVersion = "0.0.0";

    public ArtifactInfo( URL url ) {
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
        group = parts[ 0 ];
        artifactId = parts[ 1 ];
        version = parts.length > 2 ? parts[ 2 ] : "LATEST";

        // version needs to be coerced into OSGI form Major.Minor.Patch.Classifier
        osgiCompatibleVersion = VersionParser.parseVersion( version ).toString();
      }
    }

    public ArtifactInfo( String group, String artifactId, String version ) {
      this.group = group;
      this.artifactId = artifactId;
      this.version = version;
    }

    public String getGroup() {
      return group;
    }

    public String getArtifactId() {
      return artifactId;
    }

    public String getVersion() {
      return version;
    }

    public String getOsgiCompatibleVersion() {
      return osgiCompatibleVersion;
    }
  }

  /**
   * Created by nbaker on 11/25/14.
   */
  public static class VersionParser {
    private static Logger logger = LoggerFactory.getLogger( VersionParser.class );

    private static Version DEFAULT = new Version( 0, 0, 0 );
    private static Pattern VERSION_PAT = Pattern.compile( "([0-9]+)?(?:\\.([0-9]*)(?:\\.([0-9]*))?)?[\\.-]?(.*)" );
    private static Pattern CLASSIFIER_PAT = Pattern.compile( "[a-zA-Z0-9_\\-]+" );

    public static Version parseVersion( String incomingVersion ) {
      if ( StringUtils.isEmpty( incomingVersion ) ) {
        return DEFAULT;
      }
      Matcher m = VERSION_PAT.matcher( incomingVersion );
      if ( !m.matches() ) {
        return DEFAULT;
      } else {
        String s_major = m.group( 1 );
        String s_minor = m.group( 2 );
        String s_patch = m.group( 3 );
        String classifier = m.group( 4 );
        Integer major = 0;
        Integer minor = 0;
        Integer patch = 0;

        if ( !StringUtils.isEmpty( s_major ) ) {
          try {
            major = Integer.parseInt( s_major );
          } catch ( NumberFormatException e ) {
            logger.warn( "Major version part not an integer: " + s_major );
          }
        }

        if ( !StringUtils.isEmpty( s_minor ) ) {
          try {
            minor = Integer.parseInt( s_minor );
          } catch ( NumberFormatException e ) {
            logger.warn( "Minor version part not an integer: " + s_minor );
          }
        }

        if ( !StringUtils.isEmpty( s_patch ) ) {
          try {
            patch = Integer.parseInt( s_patch );
          } catch ( NumberFormatException e ) {
            logger.warn( "Patch version part not an integer: " + s_patch );
          }
        }

        if ( classifier != null ) {
          // classifiers cannot have a '.'
          classifier = classifier.replaceAll( "\\.", "_" );

          // Classifier characters must be in the following ranges a-zA-Z0-9_\-
          if ( !CLASSIFIER_PAT.matcher( classifier ).matches() ) {
            logger.warn( "Provided Classifier not valid for OSGI, ignoring" );
            classifier = null;
          }
        }
        if ( classifier != null ) {
          return new Version( major, minor, patch, classifier );
        } else {
          return new Version( major, minor, patch );
        }

      }
    }
  }
}
