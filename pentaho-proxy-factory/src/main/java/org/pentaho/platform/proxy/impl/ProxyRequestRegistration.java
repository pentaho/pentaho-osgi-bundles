package org.pentaho.platform.proxy.impl;

/**
 * Created by nbaker on 8/10/15.
 */
public class ProxyRequestRegistration {
  private Class<?> clazz;

  public ProxyRequestRegistration( Class<?> clazz ) {
    this.clazz = clazz;
  }

  public Class<?> getClassForProxying() {
    return this.clazz;
  }
}
