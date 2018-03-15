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
package org.pentaho.webpackage.extender.http.impl;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.pentaho.requirejs.RequireJsPackage;
import org.pentaho.webpackage.core.IPentahoWebPackage;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * This RequireJsPackage implementation handles webpackage's package.json files.
 */
public class RequireJsPackageImpl implements RequireJsPackage {
  private final BundleContext bundleContext;

  private final IPentahoWebPackage pentahoWebPackage;

  private ServiceRegistration<?> serviceReference;

  private final Map<String, String> modules;
  private final Map<String, String> packages;

  private final Map<String, String> dependencies;

  private final Map<String, String> scripts;

  private final Map<String, Map<String, ?>> config;

  private final Map<String, Map<String, String>> localMap;

  private final Map<String, Map<String, ?>> shim;

  private boolean preferGlobal;

  RequireJsPackageImpl( BundleContext bundleContext, IPentahoWebPackage pentahoWebPackage ) {
    this.bundleContext = bundleContext;

    this.pentahoWebPackage = pentahoWebPackage;

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

  private void addScript( String name, String file ) {
    this.scripts.put( name, file );
  }

  @Override
  public boolean hasScript( String name ) {
    return this.scripts.containsKey( name );
  }

  public URL getScriptResource( String name ) {
    Bundle bundle = this.bundleContext.getBundle();
    String scriptPath = this.pentahoWebPackage.getResourceRootPath() + "/" + this.scripts.get( name );

    return bundle.getResource( scriptPath );
  }

  private void addConfig( String moduleId, Map<String, ?> configuration ) {
    this.config.put( moduleId, configuration );
  }

  private void addShim( String moduleId, Map<String, ?> configuration ) {
    this.shim.put( moduleId, configuration );
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
      this.addModule( this.getName(), "/" );
    }

    this.preferGlobal = false;

    json.forEach( new BiConsumer<String, Object>() {
      @Override
      public void accept( String key, Object value ) {
        switch ( key ) {
          case "paths":
            Map<String, ?> paths = (Map<String, ?>) value;

            paths.forEach( ( moduleId, path ) -> {
              if ( path instanceof String ) {
                addModule( moduleId, (String) path );
              }
            } );

            break;
          case "packages":
            List<Object> packages = (List<Object>) value;

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

            break;
          case "dependencies":
            Map<String, ?> dependencies = (Map<String, ?>) value;

            dependencies.forEach( ( packageName, version ) -> {
              if ( version instanceof String ) {
                addDependency( packageName, (String) version );
              }
            } );

            break;
          case "scripts":
            Map<String, ?> scripts = (Map<String, ?>) value;

            scripts.forEach( ( name, file ) -> {
              if ( file instanceof String ) {
                addScript( name, (String) file );
              }
            } );

            break;
          case "config":
            Map<String, ?> config = (Map<String, ?>) value;

            config.forEach( ( moduleId, configuration ) -> {
              if ( configuration instanceof Map ) {
                addConfig( moduleId, (Map<String, ?>) configuration );
              }
            } );

            break;
          case "map":
            Map<String, Map<String, ?>> mappings = (Map<String, Map<String, ?>>) value;
            mappings.forEach( ( where, map ) -> {
              map.forEach( ( originalModuleId, mappedModuleId ) -> {
                if ( mappedModuleId instanceof String ) {
                  addMap( where, originalModuleId, (String) mappedModuleId );
                }
              } );
            } );

            break;
          case "shim":
            Map<String, ?> shim = (Map<String, ?>) value;

            shim.forEach( ( moduleId, configuration ) -> {
              if ( configuration instanceof Map ) {
                addShim( moduleId, (Map<String, ?>) configuration );
              } else if ( configuration instanceof List ) {
                Map<String, Object> deps = new HashMap<>();
                deps.put( "deps", configuration );

                addShim( moduleId, deps );
              }
            } );

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

  @Override
  public void register() {
    this.serviceReference = this.bundleContext.registerService( RequireJsPackage.class.getName(), this, null );
  }

  @Override
  public void unregister() {
    if ( this.serviceReference != null ) {
      try {
        this.serviceReference.unregister();
      } catch ( RuntimeException ignored ) {
        // service might be already unregistered automatically by the bundle lifecycle manager
      }

      this.serviceReference = null;
    }
  }
}
