/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.requirejs.impl.types;

import org.pentaho.requirejs.IRequireJsPackage;

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
 * configuration is applied globally ({@link IRequireJsPackage#preferGlobal} returns true).
 */
public class MetaInfRequireJson implements IRequireJsPackage {
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

  public MetaInfRequireJson( Map<String, Object> requireJsonObject ) {
    this.requireJsonObject = requireJsonObject;

    this.modules = new HashMap<>();
    this.packages = new HashMap<>();

    this.dependencies = new HashMap<>();

    this.config = new HashMap<>();

    this.localMap = new HashMap<>();

    this.shim = new HashMap<>();

    this.isAmdPackage = true;

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
    return this.name.isEmpty() ? "" : ( this.name + "@" + this.version );
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

  private void removeModule( String moduleId ) {
    this.modules.remove( moduleId );
    this.packages.remove( moduleId );
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

  private void removeDependency( String packageName ) {
    dependencies.remove( packageName );
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

  private void removeConfig( String moduleId ) {
    this.config.remove( moduleId );
  }

  @Override
  public Map<String, Map<String, ?>> getConfig() {
    return Collections.unmodifiableMap( this.config );
  }

  private void addMap( String where, String originalModuleId, String mappedModuleId ) {
    this.localMap.computeIfAbsent( where, m -> new HashMap<>() ).put( originalModuleId, mappedModuleId );
  }

  private void removeMap( String where ) {
    this.removeMap( where, null );
  }

  private void removeMap( String where, String originalModuleId ) {
    if ( this.localMap.containsKey( where ) ) {
      if ( originalModuleId != null ) {
        this.localMap.get( where ).remove( originalModuleId );
      } else {
        this.localMap.remove( where );
      }
    }
  }

  @Override
  public Map<String, Map<String, String>> getMap() {
    return Collections.unmodifiableMap( this.localMap );
  }

  private void addShim( String moduleId, Map<String, ?> configuration ) {
    this.shim.put( moduleId, configuration );
  }

  private void removeShim( String moduleId ) {
    this.shim.remove( moduleId );
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
      moduleIdTranslator.put( artifactId + "@" + this.version, this.name );
    }

    List<String> basePaths = new ArrayList<>();

    availableModules.forEach( ( moduleId, versions ) -> {
      versions.forEach( ( version, moduleInfo ) -> {
        moduleIdTranslator.put( moduleId + "@" + version, moduleId );

        basePaths.add( moduleId + "@" + version );
        basePaths.add( "/" + moduleId + "@" + version );

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
          String path = this.modules.getOrDefault( packageName, "/" + packageName );

          path = getUnversionedPath( basePaths, path );

          this.addModule( packageName, path, "main" );
        } else if ( packageDefinition instanceof HashMap ) {
          final HashMap<String, String> packageObj = (HashMap<String, String>) packageDefinition;

          if ( packageObj.containsKey( "name" ) ) {
            String packageName = getUnversionedModuleId( moduleIdTranslator, packageObj.get( "name" ) );
            String path = packageObj.getOrDefault( "location", "/" + ( !packageName.equals( name ) ? packageName : "" ) );
            String mainScript = packageObj.getOrDefault( "main", "main" );

            path = getUnversionedPath( basePaths, path );

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
            this.addMap( unversionedWhere, getUnversionedModuleId( moduleIdTranslator, originalModuleId ), getUnversionedModuleId( moduleIdTranslator, (String) mappedModuleId ) );
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
        } else if ( configuration instanceof List ) {
          HashMap<String, Object> cfg = new HashMap();
          cfg.put( "deps", configuration );
          this.addShim( unversionedModuleId, cfg );
        }

      } );
    }

    if ( meta != null && meta.containsKey( "overrides" ) ) {
      final Map<String, Object> overrides = (Map<String, Object>) meta.getOrDefault( "overrides", Collections.<String, Object>emptyMap() );

      if ( overrides.containsKey( "dependencies" ) ) {
        final HashMap<String, ?> dependencies = (HashMap<String, ?>) overrides.get( "dependencies" );

        dependencies.forEach( ( dependencyId, versionRequirement ) -> {
          if ( versionRequirement instanceof String ) {
            addDependency( dependencyId, (String) versionRequirement );
          } else {
            removeDependency( dependencyId );
          }
        } );
      }

      if ( overrides.containsKey( "paths" ) ) {
        Map<String, ?> paths = (Map<String, ?>) overrides.get( "paths" );
        paths.forEach( ( moduleId, path ) -> {
          if ( path instanceof String ) {
            this.addModule( moduleId, (String) path );
          } else {
            this.removeModule( moduleId );
          }
        } );
      }

      if ( overrides.containsKey( "packages" ) ) {
        List<Object> packages = (List<Object>) overrides.get( "packages" );
        packages.forEach( packageDefinition -> {
          if ( packageDefinition instanceof String ) {
            String packageName = (String) packageDefinition;

            String path = this.modules.getOrDefault( packageName, "/" + packageName );

            path = getUnversionedPath( basePaths, path );

            this.addModule( packageName, path, "main" );
          } else if ( packageDefinition instanceof HashMap ) {
            final HashMap<String, String> packageObj = (HashMap<String, String>) packageDefinition;

            if ( packageObj.containsKey( "name" ) ) {
              String packageName = packageObj.get( "name" );
              String path = packageObj.getOrDefault( "location", "/" + ( !packageName.equals( name ) ? packageName : "" )  );
              String mainScript = packageObj.getOrDefault( "main", "main" );

              path = getUnversionedPath( basePaths, path );

              this.addModule( packageName, path, mainScript );
            }
          }
        } );
      }

      if ( overrides.containsKey( "config" ) ) {
        Map<String, ?> config = (Map<String, ?>) overrides.get( "config" );
        config.forEach( ( moduleId, configuration ) -> {
          if ( configuration instanceof Map ) {
            this.addConfig( moduleId, (Map<String, ?>) configuration );
          } else {
            this.removeConfig( moduleId );
          }
        } );
      }

      if ( overrides.containsKey( "map" ) ) {
        Map<String, ?> mappings = (Map<String, ?>) overrides.get( "map" );
        mappings.forEach( ( where, map ) -> {
          if ( map instanceof Map ) {
            ( (Map<String, ?>) map ).forEach( ( originalModuleId, mappedModuleId ) -> {
              if ( mappedModuleId instanceof String ) {
                this.addMap( where, originalModuleId, (String) mappedModuleId );
              } else {
                this.removeMap( where, originalModuleId );
              }
            } );
          } else {
            this.removeMap( where );
          }
        } );
      }

      if ( overrides.containsKey( "shim" ) ) {
        Map<String, ?> config = (Map<String, ?>) overrides.get( "shim" );
        config.forEach( ( originalModuleId, configuration ) -> {
          if ( configuration instanceof Map ) {
            this.addShim( originalModuleId, (Map<String, ?>) configuration );
          } else {
            this.removeShim( originalModuleId );
          }
        } );
      }
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
}
