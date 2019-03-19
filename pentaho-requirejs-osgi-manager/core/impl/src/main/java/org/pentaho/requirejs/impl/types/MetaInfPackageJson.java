/*!
 * Copyright 2010 - 2019 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.requirejs.impl.types;

import org.pentaho.requirejs.IRequireJsPackage;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This RequireJsPackage implementation handles bundles with a META-INF/js/package.json file.
 * <p>
 * These files are historically filtered during build to inject versioned information, so this class
 * undoes the version scheme applied to moduleIDs and paths, to let pentaho-requirejs-manager apply its own.
 * <p>
 * Apart from functionality like {@code preferGlobal} and {@code scripts}, there is also a significant syntax
 * difference to webpackage's package.json files, as it only supports a single level map syntax, allowing only
 * to specify local mappings.
 */
public class MetaInfPackageJson implements IRequireJsPackage {
  private final Map<String, Object> packageJsonObject;

  private String name;
  private String version;

  private final Map<String, String> modules;
  private final Map<String, String> packages;

  private final Map<String, String> dependencies;

  private final Map<String, Map<String, ?>> config;

  private final Map<String, Map<String, String>> localMap;

  private final Map<String, Map<String, ?>> shim;

  public MetaInfPackageJson( Map<String, Object> packageJsonObject ) {
    this.packageJsonObject = packageJsonObject;

    this.modules = new HashMap<>();
    this.packages = new HashMap<>();

    this.dependencies = new HashMap<>();

    this.config = new HashMap<>();

    this.localMap = new HashMap<>();

    this.shim = new HashMap<>();

    this.init();
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getVersion() {
    return this.version;
  }

  @Override
  public String getWebRootPath() {
    return this.name + "@" + this.version;
  }

  @Override
  public boolean preferGlobal() {
    return false;
  }

  private void addModule( String moduleId, String path ) {
    this.modules.put( moduleId, path );
  }

  private void addModule( String moduleId, String path, String main ) {
    this.modules.put( moduleId, path );
    this.packages.put( moduleId, main );
  }

  @Override
  public Map<String, String> getModules() {
    return Collections.unmodifiableMap( this.modules );
  }

  @Override
  public String getModuleMainFile( String moduleId ) {
    return this.packages.get( moduleId );
  }

  private void addDependency( String packageName, String version ) {
    dependencies.put( packageName, version );
  }

  @Override
  public Map<String, String> getDependencies() {
    return Collections.unmodifiableMap( this.dependencies );
  }

  @Override
  public boolean hasScript( String name ) {
    return false;
  }

  public URL getScriptResource( String name ) {
    return null;
  }

  private void addConfig( String moduleId, Map<String, ?> configuration ) {
    this.config.put( moduleId, configuration );
  }

  @Override
  public Map<String, Map<String, ?>> getConfig() {
    return Collections.unmodifiableMap( this.config );
  }

  private void addMap( String where, String originalModuleId, String mappedModuleId ) {
    this.localMap.computeIfAbsent( where, m -> new HashMap<>() ).put( originalModuleId, mappedModuleId );
  }

  @Override
  public Map<String, Map<String, String>> getMap() {
    return Collections.unmodifiableMap( this.localMap );
  }

  private void addShim( String moduleId, Map<String, ?> configuration ) {
    this.shim.put( moduleId, configuration );
  }

  @Override
  public Map<String, Map<String, ?>> getShim() {
    return Collections.unmodifiableMap( this.shim );
  }

  private void init() {
    this.initFromPackageJson( this.packageJsonObject );
  }

  private void initFromPackageJson( Map<String, Object> json ) {
    this.name = (String) json.get( "name" );
    this.version = (String) json.get( "version" );

    String versionedModuleId = this.name + "@" + this.version;
    String versionedPath = this.name + "@" + this.version;

    // default module
    this.addModule( this.name, "/" );

    if ( json.containsKey( "paths" ) ) {
      Map<String, ?> paths = (Map<String, ?>) json.get( "paths" );
      paths.forEach( ( moduleId, path ) -> {
        if ( path instanceof String ) {
          this.addModule( moduleId, (String) path );
        }
      } );
    }

    if ( json.containsKey( "main" ) ) {
      String file = (String) json.get( "main" );
      if ( file.startsWith( "./" ) ) {
        file = file.substring( 2 );
      } else if ( file.startsWith( "/" ) ) {
        file = file.substring( 1 );
      }

      if ( file.endsWith( ".js" ) ) {
        String filename = file.substring( 0, file.length() - 3 );

        this.addModule( getName(), this.modules.get( getName() ), filename );
      }
    }

    if ( json.containsKey( "packages" ) ) {
      List<Object> packages = (List<Object>) json.get( "packages" );
      packages.forEach( packageDefinition -> {
        if ( packageDefinition instanceof String ) {
          String packageName = (String) packageDefinition;
          this.addModule( getName() + "/" + packageName, null, "main" );
        } else if ( packageDefinition instanceof HashMap ) {
          final HashMap<String, String> packageObj = (HashMap<String, String>) packageDefinition;

          if ( packageObj.containsKey( "name" ) ) {
            String packageName = packageObj.get( "name" );
            String path = packageObj.get( "location" );
            String mainScript = packageObj.getOrDefault( "main", "main" );

            if ( path != null && path.startsWith( versionedPath ) ) {
              path = path.substring( versionedPath.length() );
            }

            this.addModule( getName() + "/" + packageName, path, mainScript );
          }
        }
      } );
    }

    if ( json.containsKey( "dependencies" ) ) {
      Map<String, ?> dependencies = (Map<String, ?>) json.get( "dependencies" );

      dependencies.forEach( ( packageName, version ) -> {
        if ( version instanceof String ) {
          this.addDependency( packageName, (String) version );
        }
      } );
    }

    if ( json.containsKey( "config" ) ) {
      Map<String, ?> config = (Map<String, ?>) json.get( "config" );
      config.forEach( ( moduleId, configuration ) -> {
        if ( configuration instanceof Map ) {
          if ( moduleId.equals( "pentaho/modules" ) ) {
            // undo the use of the versioned moduleID, that will be handled later
            Map<String, Object> newConfiguration = new HashMap<>();

            ( (Map<String, ?>) configuration ).forEach( ( maybeVersionedModuleId, moduleInfo ) -> {
              String notVersionedModuleId = maybeVersionedModuleId;
              if ( maybeVersionedModuleId.startsWith( versionedModuleId ) ) {
                notVersionedModuleId = getName() + maybeVersionedModuleId.substring( versionedModuleId.length() );
              }

              newConfiguration.put( notVersionedModuleId, moduleInfo );
            } );

            this.addConfig( moduleId, newConfiguration );
          } else {
            this.addConfig( moduleId, (Map<String, ?>) configuration );
          }
        }
      } );
    }

    // package.json in META-INF uses a single level map syntax that only allows specifying local mappings
    if ( json.containsKey( "map" ) ) {
      Map<String, ?> map = (Map<String, ?>) json.get( "map" );
      map.forEach( ( originalModuleId, mappedModuleId ) -> {
        if ( mappedModuleId instanceof String ) {
          this.addMap( this.name, originalModuleId, (String) mappedModuleId );
        }
      } );
    }

    if ( json.containsKey( "shim" ) ) {
      Map<String, ?> shim = (Map<String, ?>) json.get( "shim" );
      shim.forEach( ( moduleId, configuration ) -> {
        if ( configuration instanceof Map ) {
          this.addShim( moduleId, (Map<String, ?>) configuration );
        } else if ( configuration instanceof List ) {
          Map<String, Object> deps = new HashMap<>();
          deps.put( "deps", configuration );

          this.addShim( moduleId, deps );
        }
      } );
    }
  }
}
