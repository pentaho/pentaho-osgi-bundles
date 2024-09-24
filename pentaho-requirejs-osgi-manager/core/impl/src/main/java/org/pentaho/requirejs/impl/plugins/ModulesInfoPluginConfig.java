/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
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
