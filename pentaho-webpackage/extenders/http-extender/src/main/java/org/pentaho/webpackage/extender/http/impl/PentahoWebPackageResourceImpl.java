package org.pentaho.webpackage.extender.http.impl;

import org.pentaho.webpackage.core.PentahoWebPackageResource;

public class PentahoWebPackageResourceImpl implements PentahoWebPackageResource {
  private String resourcePath;
  private ClassLoader classLoader;

  public PentahoWebPackageResourceImpl( String resourcePath, ClassLoader classLoader ) {
    this.resourcePath = resourcePath;
    this.classLoader = classLoader;
  }

  @Override
  public String getResourcePath() {
    return this.resourcePath;
  }

  @Override
  public ClassLoader getClassLoader() {
    return this.classLoader;
  }
}
