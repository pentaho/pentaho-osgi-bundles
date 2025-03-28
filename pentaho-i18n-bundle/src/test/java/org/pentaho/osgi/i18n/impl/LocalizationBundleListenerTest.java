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

package org.pentaho.osgi.i18n.impl;

import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.slf4j.Logger;
import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Created by bryan on 9/5/14.
 */
public class LocalizationBundleListenerTest {
  private Logger log;
  private LocalizationManager localizationManager;
  private Bundle bundle;
  private BundleEvent bundleEvent;
  private BundleContext bundleContext;
  private Logger cachedLogger;
  private LocalizationBundleListener localizationBundleListener;

  @Before
  public void setup() {
    log = mock( Logger.class );
    cachedLogger = LocalizationBundleListener.getLog();
    LocalizationBundleListener.setLog( log );
    localizationManager = mock( LocalizationManager.class );
    bundleEvent = mock( BundleEvent.class );
    bundle = mock( Bundle.class );
    bundleContext = mock( BundleContext.class );
    when( bundleEvent.getBundle() ).thenReturn( bundle );
    when( bundleContext.getBundle() ).thenReturn( bundle );
    localizationBundleListener = new LocalizationBundleListener();
    localizationBundleListener.setLocalizationManager( localizationManager );
    localizationBundleListener.setBundleContext( bundleContext );
  }

  @After
  public void teardown() {
    LocalizationBundleListener.setLog( cachedLogger );
  }

  @Test
  public void testBundleStart() throws IOException, ParseException {
    when( bundleEvent.getType() ).thenReturn( BundleEvent.STARTED );
    localizationBundleListener.bundleChanged( bundleEvent );
    verify( localizationManager ).bundleChanged( bundle );
  }

  @Test
  public void testBundleStop() throws IOException, ParseException {
    when( bundleEvent.getType() ).thenReturn( BundleEvent.STOPPED );
    localizationBundleListener.bundleChanged( bundleEvent );
    verify( localizationManager ).bundleChanged( bundle );
  }

  @Test
  public void testBundleOther() throws IOException, ParseException {
    when( bundleEvent.getType() ).thenReturn( BundleEvent.INSTALLED );
    localizationBundleListener.bundleChanged( bundleEvent );
    verifyNoMoreInteractions( localizationManager );
  }

  @Test
  public void testInit() throws Exception {
    when( bundle.getState() ).thenReturn( Bundle.ACTIVE );
    when( bundleContext.getBundles() ).thenReturn( new Bundle[]{bundle} );
    localizationBundleListener.init( );
    verify( localizationManager ).bundleChanged( bundle );
  }

  @Test
  public void testInitNotActive() throws Exception {
    when( bundleContext.getBundles() ).thenReturn( new Bundle[]{bundle} );
    when( bundle.getState() ).thenReturn( Bundle.STOPPING );
    localizationBundleListener.init( );
    verify( localizationManager, never() ).bundleChanged( bundle );
  }


  @Test
  public void testBundleStartException() throws IOException, ParseException {
    when( bundleEvent.getType() ).thenReturn( BundleEvent.STARTED );
    RuntimeException runtimeException = new RuntimeException( "Message" );
    doThrow( runtimeException ).when( localizationManager ).bundleChanged( bundle );
    localizationBundleListener.bundleChanged( bundleEvent );
    verify( localizationManager ).bundleChanged( bundle );
    verify( log ).error( runtimeException.getMessage(), runtimeException );
  }
}
