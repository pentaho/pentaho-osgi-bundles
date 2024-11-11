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

package org.pentaho.osgi.platform.plugin.deployer.impl;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleEvent;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created on 4/5/2017.
 */
public class BundleStateManagerTest {
  BundleStateManager bundleStateManager;
  public static final String BUNDLE_COMMON_UI = "common-ui307";

  @Before
  public void setup() {
    bundleStateManager = new BundleStateManager();
  }

  @Test
  public void testIsBundleInstalled() {
    //Set the state to installed
    bundleStateManager.setState( BUNDLE_COMMON_UI, BundleEvent.INSTALLED );
    assertTrue( bundleStateManager.isBundleInstalled( BUNDLE_COMMON_UI ) );

    //Set the state to Uninstalled
    bundleStateManager.setState( BUNDLE_COMMON_UI, BundleEvent.UNINSTALLED );
    assertFalse( bundleStateManager.isBundleInstalled( BUNDLE_COMMON_UI ) );
  }

}
