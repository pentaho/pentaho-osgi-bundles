package org.pentaho.webpackage.core;

public interface PentahoWebPackageResource {
  String getResourcePath();
  ClassLoader getClassLoader();
}
