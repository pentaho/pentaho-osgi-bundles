package org.pentaho.requirejs;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public interface IRequireJsPackageConfiguration {
  void processRequireJsPackage();
  void processDependencies( final BiFunction<String, String, IRequireJsPackageConfiguration> dependencyResolver );

  Map<String, Object> getRequireConfig( final List<IRequireJsPackageConfigurationPlugin> plugins );

  Map<String, String> getBaseModuleIdsMapping();
  Map<String, String> getModuleIdsMapping();

  IRequireJsPackage getRequireJsPackage();

  String getName();
  String getVersion();

  String getWebRootPath();

  Map<String, String> getDependencies();

  boolean hasScript( final String name );
  URL getScriptResource( final String name );
}
