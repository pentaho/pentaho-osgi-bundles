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

package org.pentaho.osgi.manager.resource.api;

import org.osgi.framework.Bundle;

/**
 * Created by krivera on 6/22/17.
 */
public interface ResourceHandler {

  /**
   * Determines whether the provided bundle has managed resources to be extracted
   *
   * @param bundle - The current blueprint bundle
   * @return {@link Boolean} if the bundle has managed resources
   */
  public boolean hasManagedResources( Bundle bundle );

  /**
   * Provided a {@link Bundle} this method handles its resources
   *
   * @param blueprintBundle The {@link Bundle} to handle its resources
   */
  public void handleManagedResources( Bundle blueprintBundle );
}
