/*!
 * Copyright 2018 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.webpackage.core.impl.osgi;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.pentaho.webpackage.core.PentahoWebPackageConstants;
import org.pentaho.webpackage.core.impl.PentahoWebPackageBundleListener;
import org.pentaho.webpackage.core.impl.TestUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActivatorTest {

  // Also tests addActiveBundles and createPentahoWebPackageService
  @Test
  public void start() {
    // arrange
    String resourceRootPath = "some/resource/path";
    BundleWiring mockBundleWiring = mock( BundleWiring.class );
    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    List<BundleCapability> bundleCapabilityList = new ArrayList<>();
    bundleCapabilityList.add( mockBundleCapability );
    Bundle mockBundle = TestUtils.createBaseMockBundle();
    PentahoWebPackageBundleListener listener = new PentahoWebPackageBundleListener();
    String mockPackageJson = "{\"name\":\"foo\",\"description\":\"A packaged foo fooer for fooing foos\",\"main\":\"foo.js\",\"man\":[\".\\/man\\/foo.1\",\".\\/man\\/bar.1\"],\"version\":\"1.2.3\"}";
    URL mockUrl = TestUtils.createMockUrlConnection( mockPackageJson );
    Map<String, Object> attributes = new HashMap<>();
    attributes.put( "root", resourceRootPath + "/" );
    when( mockBundleCapability.getAttributes() ).thenReturn( attributes );
    when( mockBundle.getResource( eq( resourceRootPath + "/package.json" ) ) )
        .thenReturn( mockUrl );

    when( mockBundleWiring.getCapabilities( PentahoWebPackageConstants.CAPABILITY_NAMESPACE ) ).thenReturn( bundleCapabilityList );
    when( mockBundle.adapt( BundleWiring.class ) ).thenReturn( mockBundleWiring );

    BundleEvent mockBundleEvent = mock( BundleEvent.class );
    doReturn( mockBundle ).when( mockBundleEvent ).getBundle();
    when( mockBundleEvent.getType() ).thenReturn( BundleEvent.STARTED );

    BundleContext mockBundleContext = mock(BundleContext.class);
    Bundle[] bundles = new Bundle[1];
    bundles[0] = mockBundle;
    doReturn( bundles ).when( mockBundleContext ).getBundles();

    Activator activator = new Activator();

    // act
    activator.start( mockBundleContext );

    // assert
    // TODO: check if pentahoWebPackageBundleListener has elements ???
  }

  @Test
  public void stop() {
    // arrange
    String resourceRootPath = "some/resource/path";
    BundleWiring mockBundleWiring = mock( BundleWiring.class );
    BundleCapability mockBundleCapability = mock( BundleCapability.class );
    List<BundleCapability> bundleCapabilityList = new ArrayList<>();
    bundleCapabilityList.add( mockBundleCapability );
    Bundle mockBundle = TestUtils.createBaseMockBundle();
    PentahoWebPackageBundleListener listener = new PentahoWebPackageBundleListener();
    String mockPackageJson = "{\"name\":\"foo\",\"description\":\"A packaged foo fooer for fooing foos\",\"main\":\"foo.js\",\"man\":[\".\\/man\\/foo.1\",\".\\/man\\/bar.1\"],\"version\":\"1.2.3\"}";
    URL mockUrl = TestUtils.createMockUrlConnection( mockPackageJson );
    Map<String, Object> attributes = new HashMap<>();
    attributes.put( "root", resourceRootPath + "/" );
    when( mockBundleCapability.getAttributes() ).thenReturn( attributes );
    when( mockBundle.getResource( eq( resourceRootPath + "/package.json" ) ) )
        .thenReturn( mockUrl );

    when( mockBundleWiring.getCapabilities( PentahoWebPackageConstants.CAPABILITY_NAMESPACE ) ).thenReturn( bundleCapabilityList );
    when( mockBundle.adapt( BundleWiring.class ) ).thenReturn( mockBundleWiring );

    BundleEvent mockBundleEvent = mock( BundleEvent.class );
    doReturn( mockBundle ).when( mockBundleEvent ).getBundle();
    when( mockBundleEvent.getType() ).thenReturn( BundleEvent.STARTED );

    BundleContext mockBundleContext = mock(BundleContext.class);
    Bundle[] bundles = new Bundle[1];
    bundles[0] = mockBundle;
    doReturn( bundles ).when( mockBundleContext ).getBundles();

    Activator activator = new Activator();

    // act
    activator.start( mockBundleContext );
    activator.stop( mockBundleContext );

    // assert
    // TODO: Check if pentahoWebPackageBundleListener is null?
  }

}