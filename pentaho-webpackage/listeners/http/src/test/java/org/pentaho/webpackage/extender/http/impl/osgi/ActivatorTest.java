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
package org.pentaho.webpackage.extender.http.impl.osgi;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ActivatorTest {
  private Activator activator;

  @Before
  public void setUp() throws Exception {
    this.activator = new Activator();
  }

  @Test
  public void start() throws Exception {
    BundleContext mockBundleContext = mock( BundleContext.class );
    this.activator.start( mockBundleContext );
    ServiceTracker mockServiceTracker = spy(activator.pentahoWebPackageServiceTracker);
    verify( mockServiceTracker, times( 1 ) ).open( true );
  }

  @Test
  public void stop() throws Exception {
    BundleContext mockBundleContext = mock( BundleContext.class );
    this.activator.start( mockBundleContext );
    ServiceTracker mockServiceTracker = spy(activator.pentahoWebPackageServiceTracker);
    this.activator.stop(mockBundleContext);

    verify( mockServiceTracker, times( 1 ) ).open( true );
    verify( mockServiceTracker, times( 1 ) ).close();
  }
}
