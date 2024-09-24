/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
