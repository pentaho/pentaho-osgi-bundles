package org.pentaho.requirejs;

import java.util.Map;
import java.util.function.Function;

public interface IRequireJsPackageConfigurationPlugin {
  void apply( IRequireJsPackageConfiguration requireJsPackageConfig,
              Function<String, IRequireJsPackageConfiguration> dependencyResolver,
              Function<String, String> resolveModuleId,
              Map<String, ?> requireConfig );
}
