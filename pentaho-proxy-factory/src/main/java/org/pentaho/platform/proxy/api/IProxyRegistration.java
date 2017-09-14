/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
 */

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
