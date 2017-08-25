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
