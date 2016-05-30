/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.osgi.i18n.resource;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by bryan on 9/5/14.
 */
public class OSGIResourceBundleCacheCallableTest {
  public String name = "";

  @Test
  public void testCallable() throws Exception {
    Map<Long, Map<String, OSGIResourceBundleFactory>> configMap =
      new HashMap<Long, Map<String, OSGIResourceBundleFactory>>();
    String key = "bundle/messages";
    String frenchSuffix = "_fr_FR";
    Map<String, OSGIResourceBundleFactory> bundle1Map = new HashMap<String, OSGIResourceBundleFactory>();
    configMap.put( 1L, bundle1Map );
    OSGIResourceBundleFactory defaultFactory = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle defaultBundle = mock( OSGIResourceBundle.class );
    when( defaultBundle.getDefaultName() ).thenReturn( key );
    bundle1Map.put( key, defaultFactory );
    when( defaultFactory.getBundle( any( ResourceBundle.class ) ) ).thenReturn( defaultBundle );
    when( defaultFactory.getPropertyFilePath() ).thenReturn( key + ".properties" );

    Map<String, OSGIResourceBundleFactory> bundle2Map = new HashMap<String, OSGIResourceBundleFactory>();
    configMap.put( 2L, bundle2Map );
    OSGIResourceBundleFactory frenchFactory = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle frenchBundle = mock( OSGIResourceBundle.class );
    when( frenchBundle.getDefaultName() ).thenReturn( key + frenchSuffix);
    bundle2Map.put( key, frenchFactory );
    when( frenchFactory.getBundle( any( ResourceBundle.class ) ) ).thenReturn( frenchBundle );
    when( frenchFactory.getPropertyFilePath() ).thenReturn( key + frenchSuffix + ".properties" );

    OSGIResourceBundleCacheCallable osgiResourceBundleCacheCallable = new OSGIResourceBundleCacheCallable( configMap );
    Map<String, OSGIResourceBundle> result = osgiResourceBundleCacheCallable.call();
    verify( frenchFactory ).getBundle( defaultBundle );
    assertEquals( 2, result.size() );
    OSGIResourceBundle bundle = result.get( key );
    assertNotNull( bundle );
    assertEquals( defaultBundle, bundle );
    OSGIResourceBundle bundleFr = result.get( key + frenchSuffix );
    assertNotNull( bundleFr );
    assertEquals( frenchBundle, bundleFr );
  }


  @Test
  public void testCallablePriority() throws Exception {
    Map<Long, Map<String, OSGIResourceBundleFactory>> configMap =
      new HashMap<Long, Map<String, OSGIResourceBundleFactory>>();
    String key = "bundle/messages";
    String frenchSuffix = "_fr_FR";
    Map<String, OSGIResourceBundleFactory> bundle1Map = new HashMap<String, OSGIResourceBundleFactory>();
    configMap.put( 1L, bundle1Map );
    OSGIResourceBundleFactory defaultFactory = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle defaultBundle = mock( OSGIResourceBundle.class );
    when( defaultBundle.getDefaultName() ).thenReturn( key );
    bundle1Map.put( key, defaultFactory );
    when( defaultFactory.getBundle( any( ResourceBundle.class ) ) ).thenReturn( defaultBundle );
    when( defaultFactory.getPropertyFilePath() ).thenReturn( key + ".properties" );

    Map<String, OSGIResourceBundleFactory> bundle2Map = new HashMap<String, OSGIResourceBundleFactory>();
    configMap.put( 2L, bundle2Map );
    OSGIResourceBundleFactory frenchFactory = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle frenchBundle = mock( OSGIResourceBundle.class );
    when( frenchBundle.getDefaultName() ).thenReturn( key + frenchSuffix);
    bundle2Map.put( key, frenchFactory );
    when( frenchFactory.getBundle( any( ResourceBundle.class ) ) ).thenReturn( frenchBundle );
    when( frenchFactory.getPropertyFilePath() ).thenReturn( key + frenchSuffix + ".properties" );
    when( frenchFactory.getPriority() ).thenReturn( 5 );

    Map<String, OSGIResourceBundleFactory> bundle3Map = new HashMap<String, OSGIResourceBundleFactory>();
    configMap.put( 3L, bundle3Map );
    OSGIResourceBundleFactory frenchFactory2 = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle frenchBundle2 = mock( OSGIResourceBundle.class );
    when( frenchBundle2.getDefaultName() ).thenReturn( key + frenchSuffix);
    bundle3Map.put( key, frenchFactory2 );
    when( frenchFactory2.getBundle( any( ResourceBundle.class ) ) ).thenReturn( frenchBundle2 );
    when( frenchFactory2.getPropertyFilePath() ).thenReturn( key + frenchSuffix + ".properties" );
    when( frenchFactory2.getPriority() ).thenReturn( 11 );

    Map<String, OSGIResourceBundleFactory> bundle4Map = new HashMap<String, OSGIResourceBundleFactory>();
    configMap.put( 4L, bundle4Map );
    OSGIResourceBundleFactory frenchFactory3 = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle frenchBundle3 = mock( OSGIResourceBundle.class );
    when( frenchBundle3.getDefaultName() ).thenReturn( key + frenchSuffix);
    bundle4Map.put( key, frenchFactory3 );
    when( frenchFactory3.getBundle( any( ResourceBundle.class ) ) ).thenReturn( frenchBundle3 );
    when( frenchFactory3.getPropertyFilePath() ).thenReturn( key + frenchSuffix + ".properties" );
    when( frenchFactory3.getPriority() ).thenReturn( 8 );

    OSGIResourceBundleCacheCallable osgiResourceBundleCacheCallable = new OSGIResourceBundleCacheCallable( configMap );
    Map<String, OSGIResourceBundle> result = osgiResourceBundleCacheCallable.call();
    verify( frenchFactory2 ).getBundle( defaultBundle );
    verify( frenchFactory, never() ).getBundle( any( ResourceBundle.class ) );
    verify( frenchFactory3, never() ).getBundle( any( ResourceBundle.class ) );
    assertEquals( 2, result.size() );
    OSGIResourceBundle bundle = result.get( key );
    assertNotNull( bundle );
    assertEquals( defaultBundle, bundle );
    OSGIResourceBundle bundleFr = result.get( key + frenchSuffix );
    assertNotNull( bundleFr );
    assertEquals( frenchBundle2, bundleFr );
  }

  @Test
  public void testCallableNoDefault() throws Exception {
    Map<Long, Map<String, OSGIResourceBundleFactory>> configMap =
      new HashMap<Long, Map<String, OSGIResourceBundleFactory>>();
    String key = "bundle/messages";
    String frenchSuffix = "_fr_FR";

    Map<String, OSGIResourceBundleFactory> bundle2Map = new HashMap<String, OSGIResourceBundleFactory>();
    configMap.put( 2L, bundle2Map );
    OSGIResourceBundleFactory frenchFactory = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle frenchBundle = mock( OSGIResourceBundle.class );
    when( frenchBundle.getDefaultName() ).thenReturn( key );
    bundle2Map.put( key, frenchFactory );
    when( frenchFactory.getBundle( any( ResourceBundle.class ) ) ).thenReturn( frenchBundle );
    when( frenchFactory.getPropertyFilePath() ).thenReturn( key + frenchSuffix + ".properties" );

    OSGIResourceBundleCacheCallable osgiResourceBundleCacheCallable = new OSGIResourceBundleCacheCallable( configMap );
    Map<String, OSGIResourceBundle> result = osgiResourceBundleCacheCallable.call();
    verify( frenchFactory ).getBundle( null );
    assertEquals( 1, result.size() );
    OSGIResourceBundle bundle = result.get( key );
    assertNotNull( bundle );
    assertEquals( frenchBundle, bundle );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testIllegalBundleName() throws Exception {
    Map<Long, Map<String, OSGIResourceBundleFactory>> configMap =
      new HashMap<Long, Map<String, OSGIResourceBundleFactory>>();
    String key = "test-plugin";
    Map<String, OSGIResourceBundleFactory> bundle1Map = new HashMap<String, OSGIResourceBundleFactory>();
    configMap.put( 1L, bundle1Map );
    OSGIResourceBundleFactory defaultFactory = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle defaultBundle = mock( OSGIResourceBundle.class );
    bundle1Map.put( key, defaultFactory );
    when( defaultFactory.getBundle( any( ResourceBundle.class ) ) ).thenReturn( defaultBundle );
    when( defaultFactory.getPropertyFilePath() ).thenReturn( key + ".properties" );

    OSGIResourceBundleCacheCallable osgiResourceBundleCacheCallable = new OSGIResourceBundleCacheCallable( configMap );
    osgiResourceBundleCacheCallable.call();
  }
}
