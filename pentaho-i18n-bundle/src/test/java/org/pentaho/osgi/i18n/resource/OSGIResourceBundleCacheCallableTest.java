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

package org.pentaho.osgi.i18n.resource;

import org.junit.Test;
import org.pentaho.osgi.i18n.settings.OSGIResourceNameComparator;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

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
    Map<String, OSGIResourceBundleFactory> bundle1Map =
      new TreeMap<String, OSGIResourceBundleFactory>( new OSGIResourceNameComparator() );
    configMap.put( 1L, bundle1Map );
    OSGIResourceBundleFactory defaultFactory = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle defaultBundle = mock( OSGIResourceBundle.class );
    when( defaultBundle.getDefaultName() ).thenReturn( key );
    when( defaultFactory.getBundle( any( ResourceBundle.class ) ) ).thenReturn( defaultBundle );
    when( defaultFactory.getPropertyFilePath() ).thenReturn( key + ".properties" );
    bundle1Map.put( defaultFactory.getPropertyFilePath(), defaultFactory );

    Map<String, OSGIResourceBundleFactory> bundle2Map =
      new TreeMap<String, OSGIResourceBundleFactory>( new OSGIResourceNameComparator() );
    configMap.put( 2L, bundle2Map );
    OSGIResourceBundleFactory frenchFactory = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle frenchBundle = mock( OSGIResourceBundle.class );
    when( frenchBundle.getDefaultName() ).thenReturn( key + frenchSuffix);
    when( frenchBundle.getParent() ).thenReturn( defaultBundle );
    when( frenchFactory.getBundle( any( ResourceBundle.class ) ) ).thenReturn( frenchBundle );
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
    when( defaultFactory.getBundle( any( ResourceBundle.class ) ) ).thenReturn( defaultBundle );
    when( defaultFactory.getBundle( any( ResourceBundle.class ) ) ).thenReturn( defaultBundle );
    when( defaultFactory.getPropertyFilePath() ).thenReturn( key + ".properties" );
    bundle1Map.put( defaultFactory.getPropertyFilePath() , defaultFactory );

    Map<String, OSGIResourceBundleFactory> bundle2Map =
      new TreeMap<String, OSGIResourceBundleFactory>( new OSGIResourceNameComparator() );
    configMap.put( 2L, bundle2Map );
    OSGIResourceBundleFactory frenchFactory = mock( OSGIResourceBundleFactory.class );
    OSGIResourceBundle frenchBundle = mock( OSGIResourceBundle.class );
    when( frenchBundle.getDefaultName() ).thenReturn( key + frenchSuffix);
    when( frenchBundle.getParent() ).thenReturn( defaultBundle );
    when( frenchFactory.getBundle( any( ResourceBundle.class ) ) ).thenReturn( frenchBundle );
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
    when( frenchFactory2.getBundle( any( ResourceBundle.class ) ) ).thenReturn( frenchBundle2 );
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
    when( frenchFactory3.getBundle( any( ResourceBundle.class ) ) ).thenReturn( frenchBundle3 );
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
    when( frenchFactory.getBundle( any( ResourceBundle.class ) ) ).thenReturn( frenchBundle );
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
