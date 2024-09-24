/*!
 * Copyright 2018 - 2019 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.requirejs.impl;

import org.pentaho.requirejs.IRequireJsPackage;
import org.pentaho.requirejs.IRequireJsPackageConfiguration;
import org.pentaho.requirejs.IRequireJsPackageConfigurationPlugin;
import org.pentaho.requirejs.impl.utils.JsonMerger;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;

public class RequireJsPackageConfigurationImpl implements IRequireJsPackageConfiguration {
  private final JsonMerger merger = new JsonMerger();

  private final IRequireJsPackage requireJsPackage;

  private Map<String, String> baseModuleIdsMappings;
  private Map<String, String> baseModuleIdsMappingsWithDependencies;

  private Map<String, String> paths;
  private List<Object> packages;
  private Map<String, Map<String, ?>> shim;

  private Map<String, IRequireJsPackageConfiguration> dependencyCache;

  public RequireJsPackageConfigurationImpl( IRequireJsPackage requireJsPackage ) {
    if ( requireJsPackage == null ) {
      throw new IllegalArgumentException( "requireJsPackage is mandatory" );
    }

    this.requireJsPackage = requireJsPackage;

    this.processRequireJsPackage();
  }

  /**
   * Prepare everything that can be done without the resolved dependencies information, specially the base moduleIDs mappings,
   * required in all RequireJsPackageConfiguration before getRequireConfig is called on each.
   * <p>
   * It also resets all dependencies related information processed earlier, as it is probably invalid and {@link #processDependencies(BiFunction)}
   * will be called soon.
   */
  @Override
  public void processRequireJsPackage() {
    this.dependencyCache = null;
    this.baseModuleIdsMappingsWithDependencies = null;

    this.paths = new HashMap<>();
    this.packages = new ArrayList<>();
    this.baseModuleIdsMappings = new HashMap<>();
    this.shim = new HashMap<>();

    Map<String, String> modules = this.requireJsPackage.getModules();
    if ( modules != null ) {
      modules.forEach( ( moduleId, path ) -> {
        String versionedModuleId = initVersionedModuleId( moduleId );

        if ( path != null ) {
          String versionedPath = getVersionedPath( path );
          this.paths.put( versionedModuleId, versionedPath );
        }

        String moduleMainFile = this.requireJsPackage.getModuleMainFile( moduleId );
        if ( moduleMainFile != null ) {
          if ( !moduleMainFile.equals( "main" ) ) {
            Map<String, String> packageDefinition = new HashMap<>();
            packageDefinition.put( "name", versionedModuleId );
            packageDefinition.put( "main", moduleMainFile );
            this.packages.add( packageDefinition );
          } else {
            this.packages.add( versionedModuleId );
          }
        }
      } );
    }

    // I'm guessing it only makes sense to configure shims for modules included in the package,
    // so the dependencies moduleId mapping isn't needed.
    // Move the moduleId versioning to bellow if that happens to not be the case.
    Map<String, Map<String, ?>> shim = this.requireJsPackage.getShim();
    if ( shim != null ) {
      shim.forEach( ( moduleId, configuration ) -> this.shim.put( getVersionedModuleId( moduleId, this.baseModuleIdsMappings ), merger.clone( configuration ) ) );
    }
  }

  @Override
  public void processDependencies( BiFunction<String, String, IRequireJsPackageConfiguration> dependencyResolver ) {
    this.dependencyCache = new HashMap<>();

    this.baseModuleIdsMappingsWithDependencies = new HashMap<>();

    Map<String, String> dependencies = this.getDependencies();
    for ( String dependencyPackageName : dependencies.keySet() ) {
      String dependencyPackageVersion = dependencies.get( dependencyPackageName );

      final IRequireJsPackageConfiguration dependencyResolvedVersion = dependencyResolver.apply( dependencyPackageName, dependencyPackageVersion );

      // TODO Should we fail if dependency is not resolved?
      if ( dependencyResolvedVersion != null ) {
        Map<String, String> dependencyBaseModuleIds = dependencyResolvedVersion.getBaseModuleIdsMapping();

        dependencyBaseModuleIds.forEach( this.baseModuleIdsMappingsWithDependencies::put );

        this.dependencyCache.put( dependencyPackageName, dependencyResolvedVersion );
      }
    }

    this.baseModuleIdsMappings.forEach( this.baseModuleIdsMappingsWithDependencies::put );
  }

  @Override
  public Map<String, Object> getRequireConfig( List<IRequireJsPackageConfigurationPlugin> plugins ) {
    Map<String, Object> requireConfig = new HashMap<>();

    requireConfig.put( "paths", Collections.unmodifiableMap( this.paths ) );

    requireConfig.put( "packages", Collections.unmodifiableList( this.packages ) );

    Map<String, Map<String, String>> topMap = new HashMap<>();

    if ( !this.baseModuleIdsMappingsWithDependencies.isEmpty() ) {
      this.requireJsPackage.getModules().forEach( ( moduleId, path ) -> {
        String versionedModuleId = getVersionedModuleId( moduleId, this.baseModuleIdsMappings );

        HashMap<String, String> mappings = new HashMap<>();
        this.baseModuleIdsMappingsWithDependencies.forEach( mappings::put );

        topMap.put( versionedModuleId, mappings );
      } );
    }

    // We aren't allowing to "inject" mappings into dependencies mappings.
    // If that turns out to be something useful, we just need to change
    // this.baseModuleIdsMappings to this.baseModuleIdsMappingsWithDependencies.
    this.requireJsPackage.getMap().forEach( ( moduleId, localMappings ) -> {
      String versionedModuleId = getVersionedModuleId( moduleId, this.baseModuleIdsMappings );

      Map<String, String> mappings = topMap.computeIfAbsent( versionedModuleId, m -> new HashMap<>() );
      localMappings.forEach( ( key, value ) -> {
        String versionedValue = getVersionedModuleId( value, this.baseModuleIdsMappingsWithDependencies );

        mappings.put( key, versionedValue );
      } );
    } );

    requireConfig.put( "map", Collections.unmodifiableMap( topMap ) );

    Map<String, Map<String, ?>> config = new HashMap<>();
    this.requireJsPackage.getConfig().forEach( ( moduleId, configuration ) -> {
      config.put( moduleId, merger.clone( configuration ) );
    } );

    // modifiable map, for Configuration Plugins
    requireConfig.put( "config", config );

    Map<String, Map<String, ?>> concreteShim = new HashMap<>();
    this.shim.forEach( ( moduleId, configuration ) -> {
      HashMap<String, Object> shimConfig = new HashMap<>();

      Map<String, ?> originalModuleShimConfiguration = shim.get( moduleId );
      for ( String key : originalModuleShimConfiguration.keySet() ) {
        Object originalValue = originalModuleShimConfiguration.get( key );
        Object convertedValue = originalValue;

        if ( key.equals( "deps" ) ) {
          List<String> originalDeps = (List<String>) originalValue;
          List<String> convertedDeps = new ArrayList<>();

          for ( String depModuleId : originalDeps ) {
            String versionedDepModuleId = getVersionedModuleId( depModuleId, this.baseModuleIdsMappingsWithDependencies );
            convertedDeps.add( versionedDepModuleId );
          }

          convertedValue = convertedDeps;
        }

        shimConfig.put( key, convertedValue );
      }

      concreteShim.put( moduleId, shimConfig );
    } );

    // modifiable map, for Configuration Plugins
    requireConfig.put( "shim", concreteShim );

    if ( plugins != null ) {
      plugins.forEach( plugin -> {
        try {
          plugin.apply( this, this.dependencyCache::get, moduleId -> getVersionedModuleId( moduleId, this.baseModuleIdsMappingsWithDependencies ), Collections.unmodifiableMap( requireConfig ) );
        } catch ( Exception ignored ) {
        }
      } );
    }

    // lock config and shim after plugins
    Map<String, Map<String, ?>> mappedConfig = new HashMap<>();
    ( (Map<String, Map<String, ?>>) requireConfig.get( "config" ) ).forEach( ( moduleId, configuration ) -> {
      mappedConfig.put( getVersionedModuleId( moduleId, this.baseModuleIdsMappingsWithDependencies ), configuration );
    } );
    requireConfig.put( "config", Collections.unmodifiableMap( mappedConfig ) );

    requireConfig.put( "shim", Collections.unmodifiableMap( ( (Map<String, Map<String, ?>>) requireConfig.get( "shim" ) ) ) );

    return Collections.unmodifiableMap( requireConfig );
  }

  private String initVersionedModuleId( final String moduleId ) {
    if ( !this.preferGlobal() ) {
      String versionedName = this.getName() + "@" + this.getVersion();

      String versionedModuleId = versionedName + "/" + moduleId;

      this.baseModuleIdsMappings.put( moduleId, versionedModuleId );

      return versionedModuleId;
    }

    return moduleId;
  }

  private String getVersionedModuleId( String moduleId, Map<String, String> moduleIdsMappings ) {
    if ( moduleId.contains( "!" ) ) {
      List<String> parts = Arrays.asList( moduleId.split( "!", 2 ) );

      return getVersionedModuleId( parts.get( 0 ), moduleIdsMappings ) + "!" + getVersionedModuleId( parts.get( 1 ), moduleIdsMappings );
    }

    try {
      String baseModuleId = moduleId;
      if ( !moduleIdsMappings.containsKey( moduleId ) ) {
        baseModuleId = moduleIdsMappings.keySet().stream().filter( moduleId::startsWith ).max( Comparator.comparingInt( String::length ) ).get();
      }

      String versionedBaseModuleId = moduleIdsMappings.get( baseModuleId );
      String moduleIdLeaf = moduleId.substring( baseModuleId.length() );
      if ( !moduleIdLeaf.isEmpty() && !moduleIdLeaf.startsWith( "/" ) ) {
        // false positive, we just caught a substring (probably some old mapping that included an hardcoded version)
        return moduleId;
      }

      return versionedBaseModuleId + moduleIdLeaf;
    } catch ( NoSuchElementException e ) {
      return moduleId;
    }
  }

  private String getVersionedPath( String path ) {
    if ( path.equals( "/" ) ) {
      return this.getWebRootPath();
    }

    if ( !path.startsWith( "/" ) ) {
      return this.getWebRootPath() + "/" + path;
    }

    return this.getWebRootPath() + path;
  }

  /**
   * The base modules IDs mapping exported by this package.
   * This mapping is built by {@link #processRequireJsPackage()} (1st stage initialization),
   * which is called by the constructor (so effectively it is always available).
   */
  @Override
  public Map<String, String> getBaseModuleIdsMapping() {
    return Collections.unmodifiableMap( this.baseModuleIdsMappings );
  }

  /**
   * The package's base modules IDs mapping and of its resolved dependencies.
   * This mapping is built by {@link #processDependencies(BiFunction)} (2st stage initialization),
   * and throws if called before.
   */
  @Override
  public Map<String, String> getModuleIdsMapping() {
    if ( this.baseModuleIdsMappingsWithDependencies == null ) {
      throw new IllegalStateException( "Configuration wasn't yet initialized with processDependencies" );
    }

    return Collections.unmodifiableMap( this.baseModuleIdsMappingsWithDependencies );
  }

  // region Access to the underlying RequireJsPackage
  @Override
  public IRequireJsPackage getRequireJsPackage() {
    return requireJsPackage;
  }

  @Override
  public String getName() {
    String name = this.requireJsPackage.getName();
    return name != null ? name : "";
  }

  @Override
  public String getVersion() {
    String version = this.requireJsPackage.getVersion();
    return version != null ? version : "";
  }

  private boolean preferGlobal() {
    return this.requireJsPackage.preferGlobal();
  }

  @Override
  public Map<String, String> getDependencies() {
    Map<String, String> dependencies = this.requireJsPackage.getDependencies();
    return dependencies != null ? Collections.unmodifiableMap( dependencies ) : Collections.EMPTY_MAP;
  }

  @Override
  public boolean hasScript( final String name ) {
    return this.requireJsPackage.hasScript( name );
  }

  @Override
  public URL getScriptResource( final String name ) {
    return this.requireJsPackage.getScriptResource( name );
  }

  @Override
  public String getWebRootPath() {
    String webRootPath = this.requireJsPackage.getWebRootPath();
    return webRootPath != null ? webRootPath.replaceAll( "^/+", "" ) : "";
  }
  // endregion
}
