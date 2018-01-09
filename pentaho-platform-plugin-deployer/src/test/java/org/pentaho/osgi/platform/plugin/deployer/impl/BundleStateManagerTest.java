/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
