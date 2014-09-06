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

package org.pentaho.osgi.i18n.resource;

import org.junit.Test;

import java.io.IOException;
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
  @Test
  public void testCallable() throws Exception {
    Map<Long, Map<String, List<OSGIResourceBundleFactory>>> configMap = new HashMap<Long, Map<String, List<OSGIResourceBundleFactory>>>(  );
    String key = "test-plugin";
    String name = "org/pentaho/osgi/resources/messages";
    String frenchSuffix = "_fr_FR";
    Map<String, List<OSGIResourceBundleFactory>> bundle1Map = new HashMap<String, List<OSGIResourceBundleFactory>>(  );
    configMap.put( 1L, bundle1Map );
    OSGIResourceBundleFactory defaultFactory = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle defaultBundle = mock( OSGIResourceBundle.class );
    bundle1Map.put( key, new ArrayList<OSGIResourceBundleFactory>( Arrays.asList(defaultFactory) ) );
    when( defaultFactory.getBundle( any( ResourceBundle.class ) ) ).thenReturn( defaultBundle );
    when( defaultFactory.getPropertyFilePath() ).thenReturn( name + ".properties" );

    Map<String, List<OSGIResourceBundleFactory>> bundle2Map = new HashMap<String, List<OSGIResourceBundleFactory>>(  );
    configMap.put( 2L, bundle2Map );
    OSGIResourceBundleFactory frenchFactory= mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle frenchBundle = mock( OSGIResourceBundle.class );
    bundle2Map.put( key, new ArrayList<OSGIResourceBundleFactory>( Arrays.asList(frenchFactory) ) );
    when( frenchFactory.getBundle( any( ResourceBundle.class ) ) ).thenReturn( frenchBundle );
    when( frenchFactory.getPropertyFilePath() ).thenReturn( name + frenchSuffix + ".properties" );

    OSGIResourceBundleCacheCallable osgiResourceBundleCacheCallable = new OSGIResourceBundleCacheCallable( configMap );
    Map<String, Map<String, OSGIResourceBundle>> result = osgiResourceBundleCacheCallable.call();
    verify( frenchFactory ).getBundle( defaultBundle );
    assertEquals( 1, result.size() );
    Map<String, OSGIResourceBundle> bundles = result.get( key );
    assertNotNull( bundles );
    assertEquals( 2, bundles.size() );
    assertEquals( defaultBundle, bundles.get( name ) );
    assertEquals( frenchBundle, bundles.get( name + frenchSuffix ) );
  }


  @Test
  public void testCallablePriority() throws Exception {
    Map<Long, Map<String, List<OSGIResourceBundleFactory>>> configMap = new HashMap<Long, Map<String, List<OSGIResourceBundleFactory>>>(  );
    String key = "test-plugin";
    String name = "org/pentaho/osgi/resources/messages";
    String frenchSuffix = "_fr_FR";
    Map<String, List<OSGIResourceBundleFactory>> bundle1Map = new HashMap<String, List<OSGIResourceBundleFactory>>(  );
    configMap.put( 1L, bundle1Map );
    OSGIResourceBundleFactory defaultFactory = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle defaultBundle = mock( OSGIResourceBundle.class );
    bundle1Map.put( key, new ArrayList<OSGIResourceBundleFactory>( Arrays.asList(defaultFactory) ) );
    when( defaultFactory.getBundle( any( ResourceBundle.class ) ) ).thenReturn( defaultBundle );
    when( defaultFactory.getPropertyFilePath() ).thenReturn( name + ".properties" );

    Map<String, List<OSGIResourceBundleFactory>> bundle2Map = new HashMap<String, List<OSGIResourceBundleFactory>>(  );
    configMap.put( 2L, bundle2Map );
    OSGIResourceBundleFactory frenchFactory= mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle frenchBundle = mock( OSGIResourceBundle.class );
    bundle2Map.put( key, new ArrayList<OSGIResourceBundleFactory>( Arrays.asList(frenchFactory) ) );
    when( frenchFactory.getBundle( any( ResourceBundle.class ) ) ).thenReturn( frenchBundle );
    when( frenchFactory.getPropertyFilePath() ).thenReturn( name + frenchSuffix + ".properties" );
    when( frenchFactory.getPriority() ).thenReturn( 10 );

    Map<String, List<OSGIResourceBundleFactory>> bundle3Map = new HashMap<String, List<OSGIResourceBundleFactory>>(  );
    configMap.put( 3L, bundle3Map );
    OSGIResourceBundleFactory frenchFactory2= mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle frenchBundle2 = mock( OSGIResourceBundle.class );
    bundle3Map.put( key, new ArrayList<OSGIResourceBundleFactory>( Arrays.asList(frenchFactory2) ) );
    when( frenchFactory2.getBundle( any( ResourceBundle.class ) ) ).thenReturn( frenchBundle2 );
    when( frenchFactory2.getPropertyFilePath() ).thenReturn( name + frenchSuffix + ".properties" );
    when( frenchFactory2.getPriority() ).thenReturn( 11 );

    OSGIResourceBundleCacheCallable osgiResourceBundleCacheCallable = new OSGIResourceBundleCacheCallable( configMap );
    Map<String, Map<String, OSGIResourceBundle>> result = osgiResourceBundleCacheCallable.call();
    verify( frenchFactory2 ).getBundle( defaultBundle );
    verify( frenchFactory, never() ).getBundle( any( ResourceBundle.class ) );
    assertEquals( 1, result.size() );
    Map<String, OSGIResourceBundle> bundles = result.get( key );
    assertNotNull( bundles );
    assertEquals( 2, bundles.size() );
    assertEquals( defaultBundle, bundles.get( name ) );
    assertEquals( frenchBundle2, bundles.get( name + frenchSuffix ) );
  }

  @Test
  public void testCallableNoDefault() throws Exception {
    Map<Long, Map<String, List<OSGIResourceBundleFactory>>> configMap = new HashMap<Long, Map<String, List<OSGIResourceBundleFactory>>>(  );
    String key = "test-plugin";
    String name = "org/pentaho/osgi/resources/messages";
    String frenchSuffix = "_fr_FR";

    Map<String, List<OSGIResourceBundleFactory>> bundle2Map = new HashMap<String, List<OSGIResourceBundleFactory>>(  );
    configMap.put( 2L, bundle2Map );
    OSGIResourceBundleFactory frenchFactory= mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle frenchBundle = mock( OSGIResourceBundle.class );
    bundle2Map.put( key, new ArrayList<OSGIResourceBundleFactory>( Arrays.asList(frenchFactory) ) );
    when( frenchFactory.getBundle( any( ResourceBundle.class ) ) ).thenReturn( frenchBundle );
    when( frenchFactory.getPropertyFilePath() ).thenReturn( name + frenchSuffix + ".properties" );

    OSGIResourceBundleCacheCallable osgiResourceBundleCacheCallable = new OSGIResourceBundleCacheCallable( configMap );
    Map<String, Map<String, OSGIResourceBundle>> result = osgiResourceBundleCacheCallable.call();
    verify( frenchFactory ).getBundle( null );
    assertEquals( 1, result.size() );
    Map<String, OSGIResourceBundle> bundles = result.get( key );
    assertNotNull( bundles );
    assertEquals( 1, bundles.size() );
    assertEquals( frenchBundle, bundles.get( name + frenchSuffix ) );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testIllegalBundleName() throws Exception {
    Map<Long, Map<String, List<OSGIResourceBundleFactory>>> configMap = new HashMap<Long, Map<String, List<OSGIResourceBundleFactory>>>(  );
    String key = "test-plugin";
    String name = "_";
    Map<String, List<OSGIResourceBundleFactory>> bundle1Map = new HashMap<String, List<OSGIResourceBundleFactory>>(  );
    configMap.put( 1L, bundle1Map );
    OSGIResourceBundleFactory defaultFactory = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle defaultBundle = mock( OSGIResourceBundle.class );
    bundle1Map.put( key, new ArrayList<OSGIResourceBundleFactory>( Arrays.asList(defaultFactory) ) );
    when( defaultFactory.getBundle( any( ResourceBundle.class ) ) ).thenReturn( defaultBundle );
    when( defaultFactory.getPropertyFilePath() ).thenReturn( name + ".properties" );

    OSGIResourceBundleCacheCallable osgiResourceBundleCacheCallable = new OSGIResourceBundleCacheCallable( configMap );
    osgiResourceBundleCacheCallable.call();
  }
}
