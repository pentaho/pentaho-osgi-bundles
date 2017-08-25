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
package org.pentaho.osgi.metastore.locator.api.impl;

import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.osgi.api.MetastoreLocatorOsgi;

/**
 * Created by tkafalas on 7/13/2017.
 * <p>
 * This class exists because two ExtensionPoint annotations are not allowed on the same class.
 */
@ExtensionPoint( id = "MetastoreLocatorNewTransExtensionPoint", extensionPointId = "TransformationCreateNew",
  description = "" )
public class MetastoreLocatorNewTransExtensionPoint extends MetastoreLocatorExtensionPoint {
  public MetastoreLocatorNewTransExtensionPoint( MetastoreLocatorOsgi metastoreLocatorOsgi ) {
    super( metastoreLocatorOsgi );
  }

}
