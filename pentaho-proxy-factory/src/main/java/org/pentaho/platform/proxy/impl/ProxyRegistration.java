/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
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
