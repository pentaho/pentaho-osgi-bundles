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
