package org.pentaho.requirejs;

import java.net.URL;
import java.util.Map;

public interface RequireJsPackage {
  String getName();
  String getVersion();

  String getWebRootPath();

  boolean preferGlobal();

  Map<String, String> getModules();
  String getModuleMainFile( String moduleId );

  Map<String, String> getDependencies();

  Map<String, Map<String, ?>> getConfig();
  Map<String, Map<String, String>> getMap();
  Map<String, Map<String, ?>> getShim();

  boolean hasScript( String name );
  URL getScriptResource( String name );

  void register();

  void unregister();
}
