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
package org.pentaho.requirejs.impl.plugins;

import org.pentaho.requirejs.IRequireJsPackageConfiguration;
import org.pentaho.requirejs.IRequireJsPackageConfigurationPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles the moduleId version mapping inside configurations for pentaho/modules.
 */
public class ModulesInfoPluginConfig implements IRequireJsPackageConfigurationPlugin {
  @Override
  public void apply( IRequireJsPackageConfiguration requireJsPackageConfig,
                     Function<String, IRequireJsPackageConfiguration> dependencyResolver,
                     Function<String, String> resolveModuleId,
                     Map<String, ?> requireConfig ) {
    if ( requireConfig.containsKey( "config" ) ) {
      Map<String, Map<String, ?>> config = (Map<String, Map<String, ?>>) requireConfig.get( "config" );

      config.forEach( ( moduleId, configuration ) -> {
        if ( moduleId.equals( "pentaho/modules" ) ) {
          Map<String, ?> processedConfiguration = convertModulesConfigurations( configuration, resolveModuleId );
          config.put( moduleId, processedConfiguration );
        }
      } );
    }
  }

  private Map<String, ?> convertModulesConfigurations( Map<String, ?> configuration, Function<String, String> resolveModuleId ) {
    HashMap<String, Object> convertedConfiguration = new HashMap<>();

    for ( String moduleId : configuration.keySet() ) {
      String versionedModuleId = resolveModuleId.apply( moduleId );

      Map<String, Object> config = (Map<String, Object>) configuration.get( moduleId );
      config.forEach( ( key, value ) -> {
        if ( value != null ) {
          if ( key.equals( "base" ) || key.equals( "type" ) ) {
            String versionedType = resolveModuleId.apply( (String) value );
            config.put( key, versionedType );
          } else if ( key.equals( "annotations" )  ) {
            Map<String, ?> processedAnnotations = convertModuleAnnotations( (Map<String, ?>) value, resolveModuleId );
            config.put( key, processedAnnotations );
          }
        }
      } );

      convertedConfiguration.put( versionedModuleId, config );
    }

    return convertedConfiguration;
  }

  private Map<String, ?> convertModuleAnnotations( Map<String, ?> annotations, Function<String, String> resolveModuleId ) {
    HashMap<String, Object> convertedAnnotations = new HashMap<>();

    for ( String moduleId : annotations.keySet() ) {
      String versionedModuleId = resolveModuleId.apply( moduleId );

      convertedAnnotations.put( versionedModuleId, annotations.get( moduleId ) );
    }

    return convertedAnnotations;
  }
}
