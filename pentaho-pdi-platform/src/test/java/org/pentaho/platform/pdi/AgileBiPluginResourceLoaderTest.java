/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.pdi;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

import java.util.MissingResourceException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

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
    assertEquals( 1, resourceLoader.findResources( getClass(), "README.md" ).size() );
    assertEquals( 0, resourceLoader.findResources( getClass(), "bogus.md" ).size() );
  }

  @Test
  public void findResources1() throws Exception {
    assertEquals( 1, resourceLoader.findResources( getClass().getClassLoader(), "README.md" ).size() );
    assertEquals( 0, resourceLoader.findResources( getClass().getClassLoader(), "bogus.md" ).size() );
  }

  @Test
  public void getResourceBundle() throws Exception {
    assertEquals( "success", resourceLoader.getResourceBundle( getClass(), "test" ).getString( "test" ) );
    try {
      resourceLoader.getResourceBundle( getClass(), "bogus" );
      fail();
    } catch ( MissingResourceException e ) { }
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

    ClassLoader classLoader = mock( ClassLoader.class, withSettings().extraInterfaces( BundleReference.class ) );
    Bundle bundle = mock( Bundle.class );
    when( bundle.getSymbolicName() ).thenReturn( "test" );
    when( ((BundleReference) classLoader).getBundle() ).thenReturn( bundle );
    assertEquals( "test", pluginResourceLoader.getSymbolicName( classLoader ) );

  }
}
