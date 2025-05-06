/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.osgi.platform.plugin.deployer.impl;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;

import java.util.Dictionary;
import java.util.Hashtable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created on 4/5/2017.
 */
public class PlatformDeployerBundleListenerTest {
  private Bundle bundle;
  private BundleEvent bundleEvent;
  private BundleContext bundleContext;
  private BundleStateManager bundleStateManager;
  private PlatformDeployerBundleListener platformDeployerBundleListener;

  @Before
  public void setup() {
    bundleEvent = mock( BundleEvent.class );
    bundle = mock( Bundle.class );
    bundleContext = mock( BundleContext.class );
    when( bundleEvent.getBundle() ).thenReturn( bundle );
    when( bundleContext.getBundle() ).thenReturn( bundle );
    bundleStateManager =  new BundleStateManager();
    platformDeployerBundleListener = new PlatformDeployerBundleListener();
    platformDeployerBundleListener.setBundleStateManager( bundleStateManager );
    platformDeployerBundleListener.setBundleContext( bundleContext );
    when( bundleEvent.getBundle().getSymbolicName() ).thenReturn( "common-ui" );
    Dictionary<String, String> headers = new Hashtable<String, String>();
    headers.put( "Bundle-Name",  "common-ui" );
    headers.put( "Bundle-Version",  "100" );
    headers.put( "Bundle-PlatformPluginName",  "common-ui" );
    when( bundleEvent.getBundle().getHeaders() ).thenReturn( headers );
  }

  @Test
  public void testBundleInstalled() {
    when( bundleEvent.getType() ).thenReturn( BundleEvent.INSTALLED );
    when( bundleEvent.getBundle().getState() ).thenReturn( BundleEvent.INSTALLED );
    platformDeployerBundleListener.bundleChanged( bundleEvent );
    assertTrue( bundleStateManager.isBundleInstalled( "common-ui100" ) );
  }

  @Test
  public void testBundleUninstalled() {
    when( bundleEvent.getType() ).thenReturn( BundleEvent.UNINSTALLED );
    when( bundleEvent.getBundle().getState() ).thenReturn( BundleEvent.UNINSTALLED );
    platformDeployerBundleListener.bundleChanged( bundleEvent );
    assertFalse( bundleStateManager.isBundleInstalled( "common-ui100" ) );
  }

}
