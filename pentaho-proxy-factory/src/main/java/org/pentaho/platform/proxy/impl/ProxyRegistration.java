package org.pentaho.platform.proxy.impl;

import org.pentaho.platform.api.engine.IPentahoObjectRegistration;
import org.pentaho.platform.proxy.api.IProxyRegistration;

/**
 * Created by nbaker on 8/14/15.
 */
public class ProxyRegistration implements IProxyRegistration {

  private IPentahoObjectRegistration registration;
  private Object proxy;

  public ProxyRegistration( IPentahoObjectRegistration registration, Object proxy ) {
    this.registration = registration;
    this.proxy = proxy;
  }

  @Override public IPentahoObjectRegistration getPentahoObjectRegistration() {
    return registration;
  }

  @Override public Object getProxyObject() {
    return proxy;
  }
}
