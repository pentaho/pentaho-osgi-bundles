/*!
 * Copyright 2010 - 2024 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.osgi.i18n.resource;

import org.junit.Test;
import org.pentaho.osgi.i18n.settings.OSGIResourceNameComparator;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    Map<String, OSGIResourceBundleFactory> bundle1Map =
      new TreeMap<String, OSGIResourceBundleFactory>( new OSGIResourceNameComparator() );
    configMap.put( 1L, bundle1Map );
    OSGIResourceBundleFactory defaultFactory = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle defaultBundle = mock( OSGIResourceBundle.class );
    when( defaultBundle.getDefaultName() ).thenReturn( key );
    when( defaultFactory.getBundle( nullable( ResourceBundle.class ) ) ).thenReturn( defaultBundle );
    when( defaultFactory.getPropertyFilePath() ).thenReturn( key + ".properties" );
    bundle1Map.put( defaultFactory.getPropertyFilePath(), defaultFactory );

    Map<String, OSGIResourceBundleFactory> bundle2Map =
      new TreeMap<String, OSGIResourceBundleFactory>( new OSGIResourceNameComparator() );
    configMap.put( 2L, bundle2Map );
    OSGIResourceBundleFactory frenchFactory = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle frenchBundle = mock( OSGIResourceBundle.class );
    when( frenchBundle.getDefaultName() ).thenReturn( key + frenchSuffix);
    when( frenchBundle.getParent() ).thenReturn( defaultBundle );
    when( frenchFactory.getBundle( nullable( ResourceBundle.class ) ) ).thenReturn( frenchBundle );
    when( frenchFactory.getPropertyFilePath() ).thenReturn( key + frenchSuffix + ".properties" );
    bundle2Map.put( frenchFactory.getPropertyFilePath(), frenchFactory );

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
    Map<String, OSGIResourceBundleFactory> bundle1Map =
      new TreeMap<String, OSGIResourceBundleFactory>( new OSGIResourceNameComparator() );
    configMap.put( 1L, bundle1Map );
    OSGIResourceBundleFactory defaultFactory = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle defaultBundle = mock( OSGIResourceBundle.class );
    when( defaultBundle.getDefaultName() ).thenReturn( key );
    when( defaultFactory.getBundle( nullable( ResourceBundle.class ) ) ).thenReturn( defaultBundle );
    when( defaultFactory.getPropertyFilePath() ).thenReturn( key + ".properties" );
    bundle1Map.put( defaultFactory.getPropertyFilePath() , defaultFactory );

    Map<String, OSGIResourceBundleFactory> bundle2Map =
      new TreeMap<String, OSGIResourceBundleFactory>( new OSGIResourceNameComparator() );
    configMap.put( 2L, bundle2Map );
    OSGIResourceBundleFactory frenchFactory = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle frenchBundle = mock( OSGIResourceBundle.class );
    when( frenchBundle.getDefaultName() ).thenReturn( key + frenchSuffix);
    when( frenchBundle.getParent() ).thenReturn( defaultBundle );
    when( frenchFactory.getBundle( nullable( ResourceBundle.class ) ) ).thenReturn( frenchBundle );
    when( frenchFactory.getPropertyFilePath() ).thenReturn( key + frenchSuffix + ".properties.5" );
    when( frenchFactory.getPriority() ).thenReturn( new Integer( 5 ) );
    bundle2Map.put( frenchFactory.getPropertyFilePath(), frenchFactory );

    Map<String, OSGIResourceBundleFactory> bundle3Map =
      new TreeMap<String, OSGIResourceBundleFactory>( new OSGIResourceNameComparator() );
    configMap.put( 4L, bundle3Map );
    OSGIResourceBundleFactory frenchFactory2 = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle frenchBundle2 = mock( OSGIResourceBundle.class );
    when( frenchBundle2.getDefaultName() ).thenReturn( key + frenchSuffix);
    when( frenchBundle2.getParent() ).thenReturn( frenchBundle );
    when( frenchFactory2.getBundle( nullable( ResourceBundle.class ) ) ).thenReturn( frenchBundle2 );
    when( frenchFactory2.getPropertyFilePath() ).thenReturn( key + frenchSuffix + ".properties.8" );
    when( frenchFactory2.getPriority() ).thenReturn( new Integer( 8 ) );
    bundle3Map.put( frenchFactory2.getPropertyFilePath(), frenchFactory2 );

    Map<String, OSGIResourceBundleFactory> bundle4Map =
      new TreeMap<String, OSGIResourceBundleFactory>( new OSGIResourceNameComparator() );
    configMap.put( 3L, bundle4Map );
    OSGIResourceBundleFactory frenchFactory3 = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle frenchBundle3 = mock( OSGIResourceBundle.class );
    when( frenchBundle3.getDefaultName() ).thenReturn( key + frenchSuffix);
    when( frenchBundle3.getParent() ).thenReturn( frenchBundle2 );
    when( frenchFactory3.getBundle( nullable( ResourceBundle.class ) ) ).thenReturn( frenchBundle3 );
    when( frenchFactory3.getPropertyFilePath() ).thenReturn( key + frenchSuffix + ".properties.11" );
    when( frenchFactory3.getPriority() ).thenReturn( new Integer( 11 ) );
    bundle4Map.put( frenchFactory3.getPropertyFilePath(), frenchFactory3 );

    OSGIResourceBundleCacheCallable osgiResourceBundleCacheCallable = new OSGIResourceBundleCacheCallable( configMap );
    Map<String, OSGIResourceBundle> result = osgiResourceBundleCacheCallable.call();
    verify( frenchFactory3 ).getBundle( frenchBundle2 );
    verify( frenchFactory2 ).getBundle( frenchBundle );
    verify( frenchFactory ).getBundle( defaultBundle );
    assertEquals( 2, result.size() );
    OSGIResourceBundle bundle = result.get( key );
    assertNotNull( bundle );
    assertEquals( defaultBundle, bundle );
    OSGIResourceBundle bundleFr = result.get( key + frenchSuffix );
    assertNotNull( bundleFr );
    assertEquals( frenchBundle3, bundleFr );
  }

  @Test
  public void testCallableNoDefault() throws Exception {
    Map<Long, Map<String, OSGIResourceBundleFactory>> configMap =
      new HashMap<Long, Map<String, OSGIResourceBundleFactory>>();
    String key = "bundle/messages";
    String frenchSuffix = "_fr_FR";

    Map<String, OSGIResourceBundleFactory> bundle2Map = new TreeMap<String, OSGIResourceBundleFactory>();
    configMap.put( 2L, bundle2Map );
    OSGIResourceBundleFactory frenchFactory = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle frenchBundle = mock( OSGIResourceBundle.class );
    when( frenchBundle.getDefaultName() ).thenReturn( key );
    when( frenchFactory.getBundle( null ) ).thenReturn( frenchBundle );
    when( frenchFactory.getPropertyFilePath() ).thenReturn( key + frenchSuffix + ".properties" );
    bundle2Map.put( frenchFactory.getPropertyFilePath(), frenchFactory );

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
    Map<String, OSGIResourceBundleFactory> bundle1Map = new TreeMap<String, OSGIResourceBundleFactory>();
    configMap.put( 1L, bundle1Map );
    OSGIResourceBundleFactory defaultFactory = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle defaultBundle = mock( OSGIResourceBundle.class );
    when( defaultFactory.getBundle( any( ResourceBundle.class ) ) ).thenReturn( defaultBundle );
    when( defaultFactory.getPropertyFilePath() ).thenReturn( key + ".properties" );
    bundle1Map.put( defaultFactory.getPropertyFilePath(), defaultFactory );

    OSGIResourceBundleCacheCallable osgiResourceBundleCacheCallable = new OSGIResourceBundleCacheCallable( configMap );
    osgiResourceBundleCacheCallable.call();
  }
}
