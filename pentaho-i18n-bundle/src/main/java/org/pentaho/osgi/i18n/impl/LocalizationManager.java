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
 * Copyright 2016 - 2017 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.osgi.i18n.impl;

import org.json.simple.parser.ParseException;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.pentaho.osgi.i18n.LocalizationService;
import org.pentaho.osgi.i18n.settings.OSGIResourceNamingConvention;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class LocalizationManager implements LocalizationService {
  private static Logger log = LoggerFactory.getLogger( LocalizationManager.class );
  private final Map<Long, Map<String, ResourceBundle>> configMap = new HashMap<>();

  // For unit tests only
  static Logger getLog() {
    return log;
  }

  // For unit tests only
  static void setLog( Logger log ) {
    LocalizationManager.log = log;
  }

  public void bundleChanged( Bundle bundle ) throws IOException, ParseException {
    synchronized ( this.configMap ) {
      this.configMap.remove( bundle.getBundleId() );
    }

    if ( bundle.getState() == Bundle.ACTIVE ) {
      List<String> webPackageRoots = getBundleRoots( bundle );
      for ( String root : webPackageRoots ) {
        addBundleResources( bundle, root );
      }
    }
  }

  @Override
  public ResourceBundle getResourceBundle( Class clazz, String key, Locale locale ) {
    if ( clazz == null ) return null;

    key = "/i18n/" + key.replaceAll( "\\.", "/" );
    return getResourceBundle( key, locale, clazz.getClassLoader() );
  }

  @Override
  public ResourceBundle getResourceBundle( Bundle bundle, String key, Locale locale ) {
    if ( bundle == null ) return null;

    key = key.replaceAll( "\\.", "/" );
    return getResourceBundle( key, locale, null );
  }

  private List<String> getBundleRoots( Bundle bundle ) {
    List<String> webPackageRoots = new ArrayList<>();
    webPackageRoots.add( OSGIResourceNamingConvention.RESOURCES_I18N_FOLDER );

    BundleWiring wiring = bundle.adapt( BundleWiring.class );
    if ( wiring != null ) {
      List<BundleCapability> capabilities = wiring.getCapabilities( CAPABILITY_NAMESPACE );
      for ( BundleCapability bundleCapability : capabilities ) {
        Map<String, Object> attributes = bundleCapability.getAttributes();

        webPackageRoots.add( (String) attributes.getOrDefault( "root", "" ) );
      }
    }

    return webPackageRoots;
  }

  private void addBundleResources( Bundle bundle, String root ) {
    Map<String, ResourceBundle> configEntry = new HashMap<>();

    final String i18nResourcePattern = "*" + OSGIResourceNamingConvention.RESOURCES_DEFAULT_EXTENSION + "*";
    Enumeration<URL> urlEnumeration = bundle.findEntries( root, i18nResourcePattern, true );
    if ( urlEnumeration != null ) {
      while ( urlEnumeration.hasMoreElements() ) {
        URL url = urlEnumeration.nextElement();
        if ( url != null ) {
          String filename = url.getFile();
          String key = getResourceKey( filename );

          // TODO what do we do about priority http://jira.pentaho.com/browse/BACKLOG-8306
          String defaultName = getResourceDefaultName( filename );
          Locale locale = getResourceLocale( filename );
          configEntry.put( key, getResourceBundle( defaultName, locale, null) );
        }
      }
    }

    if ( !configEntry.isEmpty() ) {
      synchronized ( this.configMap ) {
        this.configMap.put( bundle.getBundleId(), configEntry );
      }
    }
  }

  private ResourceBundle getResourceBundle( String baseName, Locale locale, ClassLoader classLoader ) {
    if ( classLoader == null ) return null;

    return ResourceBundle.getBundle( baseName + ".properties", locale, classLoader );
  }

  /**
   * Returns property file name without extension
   *
   * @param filename
   *
   * @return property file name without extension
   */
  private String getResourceKey( String filename ) {
    return filename.replaceAll( "\\.properties.*$", "" );
  }

  private String getResourceDefaultName( String filename ) {
    return OSGIResourceNamingConvention.getPropertyDefaultName( filename );
  }

  private Locale getResourceLocale( String filename ) {
    return OSGIResourceNamingConvention.getPropertyLocale( filename );
  }

}
