package org.pentaho.requirejs;

import java.util.Map;
import java.util.function.Function;

public interface RequireJsPackageConfigurationPlugin {
  void apply( RequireJsPackageConfiguration requireJsPackageConfig,
              Function<String, RequireJsPackageConfiguration> dependencyResolver,
              Function<String, String> resolveModuleId,
              Map<String, ?> requireConfig );
}
