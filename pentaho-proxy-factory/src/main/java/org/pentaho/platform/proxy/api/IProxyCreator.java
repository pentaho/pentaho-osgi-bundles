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


package org.pentaho.platform.proxy.api;

/**
 * Instances of this class will respond to requests to create a Proxy object for a given target. Created by nbaker on
 * 8/9/15.
 */
public interface IProxyCreator<T> {

  /**
   * Asked whether the creator supports creating proxies for the given class
   *
   * @param clazz
   * @return true if supports class
   */
  boolean supports( Class<?> clazz );

  /**
   * Call to create a Proxy Object to be used instead of the target object.
   *
   * @param target
   * @return
   */
  T create( Object target );
}
