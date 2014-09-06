/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.osgi.i18n.impl;

import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.pentaho.osgi.i18n.impl.LocalizationBundleListener;
import org.pentaho.osgi.i18n.impl.LocalizationManager;
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
    when( bundleEvent.getBundle() ).thenReturn( bundle );
    localizationBundleListener = new LocalizationBundleListener();
    localizationBundleListener.setLocalizationManager( localizationManager );
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
  public void testBundleStartException() throws IOException, ParseException {
    when( bundleEvent.getType() ).thenReturn( BundleEvent.STARTED );
    RuntimeException runtimeException = new RuntimeException( "Message" );
    doThrow( runtimeException ).when( localizationManager ).bundleChanged( bundle );
    localizationBundleListener.bundleChanged( bundleEvent );
    verify( localizationManager ).bundleChanged( bundle );
    verify( log ).error( runtimeException.getMessage(), runtimeException );
  }
}
