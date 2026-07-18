/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

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
