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
