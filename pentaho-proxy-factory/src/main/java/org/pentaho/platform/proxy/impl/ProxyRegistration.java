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
 */package org.pentaho.platform.proxy.impl;

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
