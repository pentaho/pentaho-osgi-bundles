package org.pentaho.requirejs.impl.plugins;

import org.pentaho.requirejs.IRequireJsPackageConfiguration;
import org.pentaho.requirejs.IRequireJsPackageConfigurationPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles the moduleId version mapping inside configurations for pentaho/typeInfo and pentaho/instanceInfo.
 */
public class TypeAndInstanceInfoPluginConfig implements IRequireJsPackageConfigurationPlugin {
  @Override
  public void apply( IRequireJsPackageConfiguration requireJsPackageConfig,
                     Function<String, IRequireJsPackageConfiguration> dependencyResolver,
                     Function<String, String> resolveModuleId,
                     Map<String, ?> requireConfig ) {
    if ( requireConfig.containsKey( "config" ) ) {
      Map<String, Map<String, ?>> config = (Map<String, Map<String, ?>>) requireConfig.get( "config" );

      config.forEach( ( moduleId, configuration ) -> {
        if ( moduleId.equals( "pentaho/typeInfo" ) || moduleId.equals( "pentaho/instanceInfo" ) ) {
          Map<String, ?> processedConfiguration = convertTypeAndInstanceConfigurations( configuration, resolveModuleId );
          config.put( moduleId, processedConfiguration );
        }
      } );
    }
  }

  private Map<String, ?> convertTypeAndInstanceConfigurations( Map<String, ?> configuration, Function<String, String> resolveModuleId ) {
    HashMap<String, Object> convertedConfiguration = new HashMap<>();

    for ( String moduleId : configuration.keySet() ) {
      String versionedModuleId = resolveModuleId.apply( moduleId );

      Map<String, Object> config = (Map<String, Object>) configuration.get( moduleId );
      config.forEach( ( key, value ) -> {
        if ( key.equals( "base" ) || key.equals( "type" ) ) {
          String versionedType = resolveModuleId.apply( (String) value );
          config.put( key, versionedType );
        }
      } );

      convertedConfiguration.put( versionedModuleId, config );
    }

    return convertedConfiguration;
  }
}
