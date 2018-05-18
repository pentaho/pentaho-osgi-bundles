/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.webpackage.extender.requirejs.impl;

import org.pentaho.requirejs.IRequireJsPackage;
import org.pentaho.webpackage.core.IPentahoWebPackage;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * This RequireJsPackage implementation handles webpackage's package.json files.
 */
public class RequireJsPackageImpl implements IRequireJsPackage {
  private final IPentahoWebPackage pentahoWebPackage;
  private final URI resourceRoot;

  private final Map<String, String> modules;
  private final Map<String, String> packages;

  private final Map<String, String> dependencies;

  private final Map<String, String> scripts;

  private final Map<String, Map<String, ?>> config;

  private final Map<String, Map<String, String>> localMap;

  private final Map<String, Map<String, ?>> shim;

  private boolean preferGlobal;

  public RequireJsPackageImpl( IPentahoWebPackage pentahoWebPackage, URI resourceRoot ) {
    this.pentahoWebPackage = pentahoWebPackage;
    this.resourceRoot = resourceRoot;

    this.modules = new HashMap<>();
    this.packages = new HashMap<>();

    this.dependencies = new HashMap<>();

    this.scripts = new HashMap<>();

    this.config = new HashMap<>();

    this.localMap = new HashMap<>();

    this.shim = new HashMap<>();

    this.init();
  }

  @Override
  public String getName() {
    return this.pentahoWebPackage.getName();
  }

  @Override
  public String getVersion() {
    return this.pentahoWebPackage.getVersion();
  }

  @Override
  public String getWebRootPath() {
    return this.pentahoWebPackage.getWebRootPath();
  }

  @Override
  public boolean preferGlobal() {
    return this.preferGlobal;
  }

  @Override
  public Map<String, String> getModules() {
    return Collections.unmodifiableMap( this.modules );
  }

  @Override
  public String getModuleMainFile( String moduleId ) {
    return this.packages.get( moduleId );
  }

  @Override
  public Map<String, String> getDependencies() {
    return Collections.unmodifiableMap( this.dependencies );
  }

  @Override
  public boolean hasScript( String name ) {
    return this.scripts.containsKey( name );
  }

  @Override
  public URL getScriptResource( String name ) {
    URL url = null;

    try {
      url = this.resourceRoot.resolve( this.scripts.get( name ) ).toURL();
    } catch ( MalformedURLException ignored ) {
    }

    return url;
  }

  @Override
  public Map<String, Map<String, ?>> getConfig() {
    return Collections.unmodifiableMap( this.config );
  }

  @Override
  public Map<String, Map<String, String>> getMap() {
    return Collections.unmodifiableMap( this.localMap );
  }

  @Override
  public Map<String, Map<String, ?>> getShim() {
    return Collections.unmodifiableMap( this.shim );
  }

  private void init() {
    Map<String, Object> packageJson = this.pentahoWebPackage.getPackageJson();

    this.initFromPackageJson( packageJson );
  }

  private void initFromPackageJson( Map<String, Object> json ) {
    if ( !json.containsKey( "paths" ) ) {
      // default module
      this.addModule( this.getName(), "/", mainFile( json ) );
    }

    this.preferGlobal = false;

    json.forEach( new BiConsumer<String, Object>() {
      @Override
      public void accept( String key, Object value ) {
        switch ( key ) {
          case "paths":
            Map<String, ?> paths = (Map<String, ?>) value;
            processPaths( paths );
            break;

          case "packages":
            List<Object> packages = (List<Object>) value;
            processPackages( packages );
            break;

          case "dependencies":
            Map<String, ?> dependencies = (Map<String, ?>) value;
            processDependencies( dependencies );
            break;

          case "scripts":
            Map<String, ?> scripts = (Map<String, ?>) value;
            processScripts( scripts );
            break;

          case "config":
            Map<String, ?> config = (Map<String, ?>) value;
            processConfig( config );
            break;

          case "map":
            Map<String, Map<String, ?>> mappings = (Map<String, Map<String, ?>>) value;
            processMap( mappings );
            break;

          case "shim":
            Map<String, ?> shim = (Map<String, ?>) value;
            processShim( shim );
            break;

          case "preferGlobal":
            preferGlobal = ( (Boolean) value );
            break;

          default:
            // TODO Store other free extended package.json properties, available later for RequireJsPackageConfigurationPlugins
        }
      }
    } );
  }

  private String mainFile( Map<String, Object> json ) {
    String pck = null;
    if ( json.containsKey( "main" ) ) {
      // npm: https://docs.npmjs.com/files/package.json#main
      // bower: https://github.com/bower/spec/blob/master/json.md#main
      Object value = json.get( "main" );

      if ( value instanceof String ) {
        pck = processMainField( (String) value );
      } else if ( value instanceof List ) {
        List files = (List) value;

        for ( Object file : files ) {
          final String pack = processMainField( (String) file );

          if ( pack != null ) {
            pck = pack;
            break;
          }
        }
      }
    }

    // all these alternate main file fields are due to D3 (see https://github.com/d3/d3/issues/3138)
    // and possibly other libraries
    // "module" (https://github.com/rollup/rollup/wiki/pkg.module) and
    // "jsnext:main" (https://github.com/jsforum/jsforum/issues/5)
    // are only for ES2015 modules, unsupported for now
    if ( json.containsKey( "unpkg" ) ) {
      // "unpkg" field for package.json: https://github.com/unpkg/unpkg-website/issues/63
      pck = processAlternateMainField( json.get( "unpkg" ) );
    } else if ( json.containsKey( "jsdelivr" ) ) {
      // "jsdelivr" field for package.json: https://github.com/jsdelivr/jsdelivr#configuring-a-default-file-in-packagejson
      pck = processAlternateMainField( json.get( "jsdelivr" ) );
    } else if ( json.containsKey( "browser" ) ) {
      // "browser" field for package.json: https://github.com/defunctzombie/package-browser-field-spec
      pck = processAlternateMainField( json.get( "browser" ) );
    }

    return pck;
  }

  private String processAlternateMainField( Object value ) {
    String pck = null;

    if ( value instanceof String ) {
      // alternate main - basic
      pck = processMainField( (String) value );
    } else if ( value instanceof Map ) {
      // replace specific files - advanced
      Map<String, ?> overridePaths = (Map<String, ?>) value;

      for ( String overridePath : overridePaths.keySet() ) {
        Object replaceRawValue = overridePaths.get( overridePath );

        String replaceValue;
        if ( replaceRawValue instanceof String ) {
          replaceValue = (String) replaceRawValue;

          if ( overridePath.startsWith( "./" ) ) {
            // replacing an internal file, create a path definition for it
            if ( replaceValue.startsWith( "./" ) ) {
              replaceValue = replaceValue.substring( 2 );
            }

            addModule( this.getName() + removeJsExtension( overridePath.substring( 1 ) ), "/" + removeJsExtension( replaceValue ) );
          } else {
            // replacing an external module, create a map definition for it
            if ( replaceValue.startsWith( "./" ) ) {
              replaceValue = this.getName() + replaceValue.substring( 1 );
            }

            addMap( this.getName(), removeJsExtension( overridePath ), removeJsExtension( replaceValue ) );
          }
        } else {
          // ignore a module
          // TODO: Should redirect to an empty module
          String toIgnore;
          if ( overridePath.startsWith( "./" ) ) {
            toIgnore = this.getName() + removeJsExtension( overridePath.substring( 1 ) );
          } else {
            toIgnore = removeJsExtension( overridePath );
          }

          addMap( this.getName(), toIgnore, "no-where-to-be-found" );
        }
      }
    }

    return pck;
  }

  private String processMainField( String file ) {
    if ( file.startsWith( "./" ) ) {
      file = file.substring( 2 );
    } else if ( file.startsWith( "/" ) ) {
      file = file.substring( 1 );
    }

    if ( file.endsWith( ".js" ) ) {
      return removeJsExtension( file );
    }

    return null;
  }

  private String removeJsExtension( String filename ) {
    if ( !filename.endsWith( ".js" ) ) {
      return filename;
    }

    return filename.substring( 0, filename.length() - 3 );
  }

  private void processPaths( Map<String, ?> paths ) {
    paths.forEach( ( moduleId, path ) -> {
      if ( path instanceof String ) {
        addModule( moduleId, (String) path );
      }
    } );
  }

  private void processPackages( List<Object> packages ) {
    packages.forEach( packageDefinition -> {
      if ( packageDefinition instanceof String ) {
        String packageName = (String) packageDefinition;
        addModule( packageName, modules.get( packageName ), "main" );
      } else if ( packageDefinition instanceof HashMap ) {
        final HashMap<String, String> packageObj = (HashMap<String, String>) packageDefinition;

        if ( packageObj.containsKey( "name" ) ) {
          String packageName = packageObj.get( "name" );
          String path = packageObj.getOrDefault( "location", modules.get( packageName ) );
          String mainScript = packageObj.getOrDefault( "main", "main" );

          addModule( packageName, path, mainScript );
        }
      }
    } );
  }

  private void addModule( String moduleId, String path, String main ) {
    this.modules.put( moduleId, path );
    if ( main != null ) {
      this.packages.put( moduleId, main );
    }
  }

  private void addModule( String moduleId, String path ) {
    this.addModule( moduleId, path, null );
  }


  private void processDependencies( Map<String, ?> dependencies ) {
    dependencies.forEach( ( packageName, version ) -> {
      if ( version instanceof String ) {
        addDependency( packageName, (String) version );
      }
    } );
  }

  private void addDependency( String packageName, String version ) {
    dependencies.put( packageName, version );
  }

  private void processScripts( Map<String, ?> scripts ) {
    scripts.forEach( ( name, file ) -> {
      if ( file instanceof String ) {
        addScript( name, (String) file );
      }
    } );
  }

  private void addScript( String name, String file ) {
    this.scripts.put( name, file );
  }


  private void processConfig( Map<String, ?> config ) {
    config.forEach( ( moduleId, configuration ) -> {
      if ( configuration instanceof Map ) {
        addConfig( moduleId, (Map<String, ?>) configuration );
      }
    } );
  }

  private void addConfig( String moduleId, Map<String, ?> configuration ) {
    this.config.put( moduleId, configuration );
  }

  private void processMap( Map<String, Map<String, ?>> mappings ) {
    mappings.forEach( ( where, map ) -> {
      map.forEach( ( originalModuleId, mappedModuleId ) -> {
        if ( mappedModuleId instanceof String ) {
          addMap( where, originalModuleId, (String) mappedModuleId );
        }
      } );
    } );
  }

  private void addMap( String where, String originalModuleId, String mappedModuleId ) {
    this.localMap.computeIfAbsent( where, m -> new HashMap<>() ).put( originalModuleId, mappedModuleId );
  }

  private void processShim( Map<String, ?> shim ) {
    shim.forEach( ( moduleId, configuration ) -> {
      if ( configuration instanceof Map ) {
        addShim( moduleId, (Map<String, ?>) configuration );
      } else if ( configuration instanceof List ) {
        Map<String, Object> deps = new HashMap<>();
        deps.put( "deps", configuration );

        addShim( moduleId, deps );
      }
    } );
  }

  private void addShim( String moduleId, Map<String, ?> configuration ) {
    this.shim.put( moduleId, configuration );
  }
}
