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
package org.pentaho.requirejs.impl.types;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.pentaho.requirejs.RequireJsPackage;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This RequireJsPackage implementation handles bundles with a META-INF/js/require.json file.
 *
 * The most common such bundles are the ones generated by the pentaho-webjars-deployer.
 * Using the requirejs-osgi-meta property information, this class undoes the version scheme
 * applied to moduleIDs and paths, to let pentaho-requirejs-manager apply its own.
 *
 * There are also some bundles with handmade require.json files that don't include
 * requirejs-osgi-meta property. Those become nameless and versionless RequireJsPackages which
 * configuration is applied globally ({@link RequireJsPackage#preferGlobal} returns true).
 */
public class MetaInfRequireJson implements RequireJsPackage {
  private final BundleContext bundleContext;

  private ServiceRegistration<?> serviceReference;

  private final Map<String, Object> requireJsonObject;

  private final Map<String, String> modules;
  private final Map<String, String> packages;

  private final Map<String, String> dependencies;

  private final Map<String, Map<String, ?>> config;

  private final Map<String, Map<String, String>> localMap;

  private final Map<String, Map<String, ?>> shim;

  private String name;
  private String version;

  private boolean isAmdPackage;
  private String exports;

  public MetaInfRequireJson( BundleContext bundleContext, Map<String, Object> requireJsonObject ) {
    this.bundleContext = bundleContext;

    this.requireJsonObject = requireJsonObject;

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
    return this.name + "/" + this.version;
  }

  @Override
  public boolean preferGlobal() {
    // require.json files not generated by the webjars deployer are expected to have its config applied globally
    return this.name.isEmpty();
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
    return this.shim;
  }

  public boolean isAmdPackage() {
    return this.isAmdPackage;
  }

  public String getExports() {
    return this.exports;
  }

  private void init() {
    this.initFromRequireJson( this.requireJsonObject );
  }

  private void initFromRequireJson( Map<String, Object> json ) {
    this.name = "";
    this.version = "";

    String artifactId = null;

    HashMap<String, String> moduleIdTranslator = new HashMap<>();

    final HashMap<String, Object> meta = (HashMap<String, Object>) json.get( "requirejs-osgi-meta" );

    HashMap<String, HashMap<String, HashMap<String, ?>>> availableModules;
    if ( meta != null && meta.containsKey( "modules" ) ) {
      availableModules = (HashMap<String, HashMap<String, HashMap<String, ?>>>) meta.get( "modules" );
    } else {
      availableModules = new HashMap<>();
    }

    if ( meta != null && meta.containsKey( "artifacts" ) ) {
      HashMap<String, HashMap<String, HashMap<String, Object>>> artifacts = (HashMap<String, HashMap<String, HashMap<String, Object>>>) meta.get( "artifacts" );
      Map.Entry<String, HashMap<String, HashMap<String, Object>>> nameEntry = artifacts.entrySet().iterator().next();

      artifactId = nameEntry.getKey();
      int indexOfSlash = artifactId.lastIndexOf( '/' );
      if ( indexOfSlash != -1 ) {
        artifactId = artifactId.substring( indexOfSlash + 1 );
      }

      Map.Entry<String, HashMap<String, Object>> versionEntry = nameEntry.getValue().entrySet().iterator().next();
      this.version = versionEntry.getKey();

      HashMap<String, Object> versionInfo = versionEntry.getValue();
      if ( versionInfo.size() == 1 ) {
        this.name = versionInfo.keySet().iterator().next();
      } else {
        this.name = artifactId;
      }
    }

    if ( this.name.isEmpty() && availableModules.size() > 0 ) {
      Map.Entry<String, HashMap<String, HashMap<String, ?>>> nameEntry = availableModules.entrySet().iterator().next();
      this.name = nameEntry.getKey();

      String versionEntry = nameEntry.getValue().keySet().iterator().next();
      this.version = versionEntry;
    }

    if ( artifactId != null ) {
      moduleIdTranslator.put( artifactId + "_" + this.version, this.name );
    }

    List<String> basePaths = new ArrayList<>();

    availableModules.forEach( ( moduleId, versions ) -> {
      versions.forEach( ( version, moduleInfo ) -> {
        moduleIdTranslator.put( moduleId + "_" + version, moduleId );

        basePaths.add( moduleId + "/" + version );
        basePaths.add( "/" + moduleId + "/" + version );

        if ( moduleInfo.containsKey( "dependencies" ) ) {
          final HashMap<String, String> dependencies = (HashMap<String, String>) moduleInfo.get( "dependencies" );

          for ( String dependencyId : dependencies.keySet() ) {
            String versionRequirement = dependencies.get( dependencyId );

            addDependency( dependencyId, versionRequirement );
          }
        }

        final boolean isAmdModule = moduleInfo.containsKey( "isAmdPackage" ) && ( (Boolean) moduleInfo.get( "isAmdPackage" ) ).booleanValue();

        if ( !isAmdModule && moduleInfo.containsKey( "exports" ) ) {
          exports = (String) moduleInfo.get( "exports" );
        }

        isAmdPackage = isAmdPackage && isAmdModule;
      } );
    } );

    if ( json.containsKey( "paths" ) ) {
      Map<String, ?> paths = (Map<String, ?>) json.get( "paths" );
      paths.forEach( ( moduleId, pathObj ) -> {
        if ( pathObj instanceof String ) {
          final String path = (String) pathObj;

          final String unversionedModuleId = getUnversionedModuleId( moduleIdTranslator, moduleId );
          final String unversionedPath = getUnversionedPath( basePaths, path );

          this.addModule( unversionedModuleId, unversionedPath );
        }
      } );
    }

    if ( json.containsKey( "packages" ) ) {
      List<Object> packages = (List<Object>) json.get( "packages" );
      packages.forEach( packageDefinition -> {
        if ( packageDefinition instanceof String ) {
          String packageName = getUnversionedModuleId( moduleIdTranslator, (String) packageDefinition );

          this.addModule( packageName, null, "main" );
        } else if ( packageDefinition instanceof HashMap ) {
          final HashMap<String, String> packageObj = (HashMap<String, String>) packageDefinition;

          if ( packageObj.containsKey( "name" ) ) {
            String packageName = getUnversionedModuleId( moduleIdTranslator, packageObj.get( "name" ) );
            String path = packageObj.get( "location" );
            String mainScript = packageObj.getOrDefault( "main", "main" );

            if ( path != null ) {
              path = getUnversionedPath( basePaths, path );
            } else {
              path = this.modules.get( packageName );
            }

            this.addModule( packageName, path, mainScript );
          }
        }
      } );
    }

    if ( json.containsKey( "config" ) ) {
      Map<String, ?> config = (Map<String, ?>) json.get( "config" );
      config.forEach( ( originalModuleId, configuration ) -> {
        final String unversionedModuleId = getUnversionedModuleId( moduleIdTranslator, originalModuleId );

        if ( configuration instanceof Map ) {
          this.addConfig( unversionedModuleId, (Map<String, ?>) configuration );
        }
      } );
    }

    if ( json.containsKey( "map" ) ) {
      Map<String, Map<String, ?>> mappings = (Map<String, Map<String, ?>>) json.get( "map" );
      mappings.forEach( ( where, map ) -> {
        map.forEach( ( originalModuleId, mappedModuleId ) -> {
          final String unversionedWhere = getUnversionedModuleId( moduleIdTranslator, where );

          if ( mappedModuleId instanceof String ) {
            this.addMap( unversionedWhere, originalModuleId, (String) mappedModuleId );
          }
        } );
      } );
    }

    if ( json.containsKey( "shim" ) ) {
      Map<String, ?> config = (Map<String, ?>) json.get( "shim" );
      config.forEach( ( originalModuleId, configuration ) -> {
        final String unversionedModuleId = getUnversionedModuleId( moduleIdTranslator, originalModuleId );

        if ( configuration instanceof Map ) {
          this.addShim( unversionedModuleId, (Map<String, ?>) configuration );
        }
      } );
    }
  }

  private String getUnversionedPath( List<String> basePaths, String path ) {
    final String basePath = basePaths.stream().filter( path::startsWith ).findFirst().orElse( null );
    String unversionedPath;
    if ( basePath != null ) {
      unversionedPath = path.replaceFirst( Pattern.quote( basePath ), "" );
    } else {
      unversionedPath = path;
    }

    if ( !unversionedPath.startsWith( "/" ) ) {
      unversionedPath = "/" + unversionedPath;
    }
    return unversionedPath;
  }

  private String getUnversionedModuleId( HashMap<String, String> moduleIdTranslator, String moduleId ) {
    if ( !moduleIdTranslator.isEmpty() ) {
      final String baseModuleId = moduleIdTranslator.keySet().stream().filter( moduleId::startsWith ).findFirst().orElse( moduleId );

      String replacement = moduleIdTranslator.get( baseModuleId );
      if ( replacement != null ) {
        return moduleId.replaceFirst( Pattern.quote( baseModuleId ), replacement );
      }
    }

    return moduleId;
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
