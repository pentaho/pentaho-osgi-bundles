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

package org.pentaho.osgi.api;

/**
 * User: nbaker
 * Date: 11/30/10
 */
public interface BeanFactory {

  public Object getInstance( String id );

  public <T> T getInstance( String id, Class<T> classType );
}
