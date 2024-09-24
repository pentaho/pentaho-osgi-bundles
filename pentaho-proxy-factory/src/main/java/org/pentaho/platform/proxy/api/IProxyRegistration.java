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
package org.pentaho.platform.proxy.api;

import org.pentaho.platform.api.engine.IPentahoObjectRegistration;

/**
 * Proxy Registration holding the proxy object as well as a handle by which the proxy can be de-registered with the
 * system. Created by nbaker on 8/14/15.
 */
public interface IProxyRegistration {
  /**
   * Return the PentahoSystem ObjectFactory registration. This can be used to de-register the proxy as needed.
   *
   * @return registration
   */
  IPentahoObjectRegistration getPentahoObjectRegistration();

  /**
   * Returns the Proxy Wrapper associated with this registration
   *
   * @return proxy object
   */
  Object getProxyObject();
}
