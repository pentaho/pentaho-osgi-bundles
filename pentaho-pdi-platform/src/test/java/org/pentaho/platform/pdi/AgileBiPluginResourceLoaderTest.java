/*!
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
 * Copyright (c) 2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.pdi;

import org.apache.felix.framework.BundleWiringImpl;
import org.junit.Test;
import org.osgi.framework.Bundle;

import java.util.MissingResourceException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by nbaker on 8/15/16.
 */
public class AgileBiPluginResourceLoaderTest {
  AgileBiPluginResourceLoader resourceLoader = new AgileBiPluginResourceLoader() {
    @Override protected String getSymbolicName( Class<?> aClass ) {
      return "test";

    }

    @Override protected String getSymbolicName( ClassLoader classLoader ) {
      return "test";
    }
  };

  @Test
  public void getResourceAsBytes() throws Exception {
    assertTrue( resourceLoader.getResourceAsBytes( getClass(), "README.md" ).length > 0 );
    assertNull( resourceLoader.getResourceAsBytes( getClass(), "bogus.md" ) );
  }

  @Test
  // Currently the only one being used
  public void getResourceAsString() throws Exception {
    assertTrue( resourceLoader.getResourceAsString( this.getClass(), "README.md" )
        .contains( AgileBiPluginResourceLoader.class.getSimpleName() ) );
    assertNull( resourceLoader.getResourceAsString( this.getClass(), "bogus.txt" ) );
  }

  @Test
  public void getResourceAsString1() throws Exception {
    assertTrue( resourceLoader.getResourceAsString( getClass(), "README.md", "UTF-8" )
        .contains( AgileBiPluginResourceLoader.class.getSimpleName() ) );
    assertNull( resourceLoader.getResourceAsString( this.getClass(), "bogus.txt", "UTF-8" ) );
  }

  @Test
  public void getResourceAsStream() throws Exception {

    assertNotNull( resourceLoader.getResourceAsStream( this.getClass(), "README.md" ) );
    assertNull( resourceLoader.getResourceAsStream( this.getClass(), "bogus.txt" ) );
  }

  @Test
  public void getResourceAsStream1() throws Exception {

    assertNotNull( resourceLoader.getResourceAsStream( getClass().getClassLoader(), "README.md" ) );
    assertNull( resourceLoader.getResourceAsStream( this.getClass().getClassLoader(), "bogus.txt" ) );
  }

  @Test
  public void findResources() throws Exception {
    assertEquals(1, resourceLoader.findResources( getClass(), "README.md" ).size());
    assertEquals(0, resourceLoader.findResources( getClass(), "bogus.md" ).size());
  }

  @Test
  public void findResources1() throws Exception {
    assertEquals(1, resourceLoader.findResources( getClass().getClassLoader(), "README.md" ).size());
    assertEquals(0, resourceLoader.findResources( getClass().getClassLoader(), "bogus.md" ).size());
  }

  @Test
  public void getResourceBundle() throws Exception {
    assertEquals( "success", resourceLoader.getResourceBundle( getClass(), "test" ).getString( "test" ) );
    try {
      resourceLoader.getResourceBundle( getClass(), "bogus" );
      fail();
    } catch( MissingResourceException e ){}
  }

  @Test
  public void testGetPluginSettings() throws Exception {
    assertNull( resourceLoader.getPluginSetting( getClass(), "" ) );
    assertNull( resourceLoader.getPluginSetting( getClass(), "", "" ) );
    assertNull( resourceLoader.getPluginSetting( getClass().getClassLoader(), "", "" ) );
  }

  @Test
  public void testBundleSymbolidName() throws Exception {
    AgileBiPluginResourceLoader pluginResourceLoader = new AgileBiPluginResourceLoader();
    assertNull( pluginResourceLoader.getSymbolicName( getClass() ) );

    BundleWiringImpl.BundleClassLoader classLoader = mock( BundleWiringImpl.BundleClassLoader.class );
    Bundle bundle = mock( Bundle.class );
    when( bundle.getSymbolicName() ).thenReturn( "test" );
    when( classLoader.getBundle() ).thenReturn( bundle );
    assertEquals( "test", pluginResourceLoader.getSymbolicName( classLoader ) );

  }
}