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

import org.pentaho.platform.proxy.impl.ProxyException;

import java.util.List;
import java.util.Map;

/**
 * <p> IProxyFactory will create and register a Proxy wrapper around the given target by delegating to the most specific
 * IProxyCreator registered in the system. </p> <p> If a Proxy is created it will be registered with the PentahoSystem
 * ObjectFactory. </p> Created by nbaker on 8/9/15.
 */
public interface IProxyFactory {

  /**
   * Consults the available IProxyCreator(s) to find the most specific creator for the given target. The order of
   * creator resolution is as follows:<br/> <ul> <li>Class Hierarchy: Self->Parent->Parent's Parent->etc.</li>
   * <li>Interfaces: No guaranteed order</li> </ul> <p> If a proxy is created it will be registered with PentahoSystem
   * with the given publishedClasses and properties. An IProxyRegistration will be returned which holds the proxy object
   * as well as a IPentahoObjectRegistration handle which can be used to remove the proxy from the system as needed.
   * </p>
   *
   * @param target           Object which proxying is being requested for
   * @param publishedClasses List of classes that the proxy should be registered under in PentahoSystem
   * @param properties       Map of properties for the proxy to be registered with
   * @param <T>              Class of the Target
   * @param <K>              Unbound
   * @return IProxyRegistration holding the proxy and a handle to remove it.
   * @throws ProxyException thrown if no creator is available or if something else goes wrong in proxying
   */
  <T, K> IProxyRegistration createAndRegisterProxy( T target, List<Class<?>> publishedClasses,
                                                    Map<String, Object> properties )
      throws ProxyException;

  /**
   * Consults the available IProxyCreator(s) to find the most specific creator for the given target. The order of
   * creator resolution is as follows:<br/> <ul> <li>Class Hierarchy: Self->Parent->Parent's Parent->etc.</li>
   * <li>Interfaces: No guaranteed order</li> </ul>
   *
   * @param target           Object which proxying is being requested for
   * @param <T>              Class of the Target
   * @param <K>              Type of the returned Proxy Object
   *
   * @throws ProxyException thrown if no creator is available or if something else goes wrong in proxying
   */
  <T, K> K createProxy( T target )
      throws ProxyException;
}
