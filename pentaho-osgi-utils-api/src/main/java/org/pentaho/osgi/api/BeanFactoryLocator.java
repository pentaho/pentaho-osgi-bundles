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
 * Copyright 2015-2017 Hitachi Vantara. All rights reserved.
 */

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
