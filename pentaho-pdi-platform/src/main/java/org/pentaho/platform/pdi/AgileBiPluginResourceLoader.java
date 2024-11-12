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

import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by nbaker on 8/15/16.
 */
public class AgileBiPluginResourceLoader implements IPluginResourceLoader {
  @Override public byte[] getResourceAsBytes( Class<? extends Object> aClass, String s ) {
    try {
      return IOUtils.toByteArray( this.getResourceAsStream( aClass, s ) );
    } catch ( IOException e ) {
      return null;
    } catch ( NullPointerException e ) {
      return null;
    }
  }

  @Override public String getResourceAsString( Class<? extends Object> aClass, String s )
      throws UnsupportedEncodingException {
    return getResourceAsString( aClass, s, "UTF-8" );
  }

  protected String getSymbolicName( Class<?> aClass ) {
    return getSymbolicName( aClass.getClassLoader() );
  }

  protected String getSymbolicName( ClassLoader classLoader ) {
    if ( classLoader instanceof BundleReference ) {
      Bundle bundle = BundleReference.class.cast( classLoader ).getBundle();
      return bundle.getSymbolicName();
    } else {
      return null;
    }
  }

  @Override public String getResourceAsString( Class<? extends Object> aClass, String s, String s1 )
      throws UnsupportedEncodingException {
    try {
      return IOUtils.toString( getResourceAsStream( aClass, s ), s1 );
    } catch ( IOException e ) {
      return null;
    } catch ( NullPointerException e ) {
      return null;
    }
  }

  @Override public InputStream getResourceAsStream( Class<?> aClass, String s ) {
    String symbolicName = getSymbolicName( aClass );
    return aClass.getResourceAsStream( "/" + symbolicName + "/" + s );
  }

  @Override public InputStream getResourceAsStream( ClassLoader classLoader, String s ) {
    String symbolicName = getSymbolicName( classLoader );
    return classLoader.getResourceAsStream( symbolicName + "/" + s );
  }

  @Override public List<URL> findResources( Class<?> aClass, String s ) {
    return findResources( aClass.getClassLoader(), s );
  }

  @Override public List<URL> findResources( ClassLoader classLoader, String s ) {
    String symbolicName = getSymbolicName( classLoader );
    try {
      Enumeration<URL> enumeration = classLoader.getResources( symbolicName + "/" + s );
      List<URL> urls = new ArrayList<URL>();
      while ( enumeration.hasMoreElements() ) {
        URL nextElement = enumeration.nextElement();
        urls.add( nextElement );
      }
      return urls;
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    return Collections.emptyList();
  }

  @Override public ResourceBundle getResourceBundle( Class<?> aClass, String s ) {
    return ResourceBundle
        .getBundle( getSymbolicName( aClass ) + "/" + s, LocaleHelper.getLocale(), aClass.getClassLoader() );
  }


  // ============ These have no analogous counterpart in OSGI yet ===================
  @Override public String getPluginSetting( Class<?> aClass, String s ) {
    return null;
  }

  @Override public String getPluginSetting( Class<?> aClass, String s, String s1 ) {
    return null;
  }

  @Override public String getPluginSetting( ClassLoader classLoader, String s, String s1 ) {
    return null;
  }
}
