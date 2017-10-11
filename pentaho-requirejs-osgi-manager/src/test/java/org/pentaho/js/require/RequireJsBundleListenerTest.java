/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.js.require;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;

import static org.mockito.Mockito.*;

/**
 * Created by bryan on 8/15/14.
 */
public class RequireJsBundleListenerTest {
  private RequireJsConfigManager configManager;
  private RequireJsBundleListener requireJsBundleListener;

  @Before
  public void setup() {
    configManager = mock( RequireJsConfigManager.class );
    requireJsBundleListener = new RequireJsBundleListener( configManager );
  }

  @Test
  public void testStopped() {
    BundleEvent bundleEvent = mock( BundleEvent.class );
    Bundle bundle = mock( Bundle.class );
    when( bundleEvent.getBundle() ).thenReturn( bundle );
    when( bundleEvent.getType() ).thenReturn( BundleEvent.STOPPED );
    requireJsBundleListener.bundleChanged( bundleEvent );
    verify( configManager ).bundleChanged( bundle );
  }

  @Test
  public void testStarted() {
    BundleEvent bundleEvent = mock( BundleEvent.class );
    Bundle bundle = mock( Bundle.class );
    when( bundleEvent.getBundle() ).thenReturn( bundle );
    when( bundleEvent.getType() ).thenReturn( BundleEvent.STARTED );
    requireJsBundleListener.bundleChanged( bundleEvent );
    verify( configManager ).bundleChanged( bundle );
  }

  @Test
  public void testOther() {
    BundleEvent bundleEvent = mock( BundleEvent.class );
    Bundle bundle = mock( Bundle.class );
    when( bundleEvent.getBundle() ).thenReturn( bundle );
    when( bundleEvent.getType() ).thenReturn( BundleEvent.LAZY_ACTIVATION );
    requireJsBundleListener.bundleChanged( bundleEvent );
    verify( configManager, times( 0 ) ).bundleChanged( bundle );
  }
}
