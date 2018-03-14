package org.pentaho.requirejs;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public interface RequireJsPackageConfiguration {
  void processRequireJsPackage();
  void processDependencies( final BiFunction<String, String, RequireJsPackageConfiguration> dependencyResolver );

  Map<String, Object> getRequireConfig( final List<RequireJsPackageConfigurationPlugin> plugins );

  Map<String, String> getBaseModuleIdsMapping();
  Map<String, String> getModuleIdsMapping();

  RequireJsPackage getRequireJsPackage();

  String getName();
  String getVersion();

  String getWebRootPath();

  Map<String, String> getDependencies();

  boolean hasScript( final String name );
  URL getScriptResource( final String name );
}
