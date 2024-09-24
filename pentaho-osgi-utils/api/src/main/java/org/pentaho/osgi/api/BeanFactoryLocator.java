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

import org.osgi.framework.Bundle;

/**
 * User: nbaker
 * Date: 12/17/10
 */
public interface BeanFactoryLocator {

  /**
   * Lookup Blueprint container for given bnudle.
   *
   * @param bundle
   * @return
   */
  BeanFactory getBeanFactory( Bundle bundle );

  /**
   * The service object might already by a blueprintContainer. Return a factory with it if so.
   *
   * @param serviceObject
   * @return
   */
  BeanFactory getBeanFactory( Object serviceObject );
}
