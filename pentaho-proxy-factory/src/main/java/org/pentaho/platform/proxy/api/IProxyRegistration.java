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
