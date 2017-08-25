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
