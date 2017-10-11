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

import org.apache.karaf.bundle.core.BundleState;

/**
 * Created by bmorrise on 9/3/15.
 */
public interface BlueprintStateService {
  Boolean hasBlueprint( long bundleId );
  Boolean isBlueprintLoaded( long bundleId );
  Boolean isBlueprintTryingToLoad( long bundleId );
  Boolean isBlueprintFailed( long bundleId );
  BundleState getBundleState( long bundleId );
  String[] getBundleMissDependencies( long bundleId );
  Throwable getBundleFailureCause( long bundleId );
}
