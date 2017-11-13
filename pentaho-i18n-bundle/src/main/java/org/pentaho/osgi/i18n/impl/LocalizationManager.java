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
import org.pentaho.osgi.i18n.resource.OSGIResourceBundle;
import org.pentaho.osgi.i18n.resource.OSGIResourceBundleCacheCallable;
import org.pentaho.osgi.i18n.resource.OSGIResourceBundleFactory;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Pattern;

public class LocalizationManager implements LocalizationService {
  private static Logger log = LoggerFactory.getLogger( LocalizationManager.class );

//  private final Map<Long, Map<String, OSGIResourceBundleFactory>> configMap = new HashMap<>();
  private final Map<Long, Map<String, OSGIResourceBundle>> configMap = new HashMap<>();
//  private ExecutorService executorService;
//  private volatile Future<Map<String, OSGIResourceBundle>> cache;

  // For unit tests only
  static Logger getLog() {
    return log;
  }

  // For unit tests only
  static void setLog( Logger log ) {
    LocalizationManager.log = log;
  }

  public void setExecutorService( ExecutorService executorService ) {
//    this.executorService = executorService;
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
  public ResourceBundle getResourceBundle( Class clazz, Locale locale ) {
    String key = clazz.getPackage().getName() + ".messages";
    return this.getResourceBundle( clazz, key, locale );
  }

  @Override
  public ResourceBundle getResourceBundle( Class clazz, String key, Locale locale ) {
    key = "/i18n/" + key.replaceAll( "\\.", "/" );

    if ( clazz == null ) return this.getResourceBundle( key, locale );

    List<URL> resourceUrls = getResourceUrls( clazz, key, locale );
    return getOSGIResourceBundle( key, resourceUrls );
  }

  @Override
  public ResourceBundle getResourceBundle( Bundle bundle, String key, Locale locale ) {
    key = key.replaceAll( "\\.", "/" );

    if ( bundle == null ) return this.getResourceBundle( key, locale );

    List<URL> resourceUrls = getResourceUrls( bundle, key, locale );
    return getOSGIResourceBundle( key, resourceUrls );
  }

  @Override
  public ResourceBundle getResourceBundle( String name, Locale locale ) {
    String root = name.startsWith( "/" ) || name.startsWith( "i18n/" ) ? "" : "/i18n/";

    for ( Map<String, OSGIResourceBundle> bundleFactoryMap : this.configMap.values() ) {
      for ( OSGIResourceBundle resourceBundle : bundleFactoryMap.values() ) {
        String defaultName = resourceBundle.getDefaultName();

        for ( String candidate : OSGIResourceNamingConvention.getCandidateNames( root + name, locale ) ) {
          if ( candidate.equals( defaultName ) ) {
            return resourceBundle;
          }
        }
      }
    }

    return null;
  }

  @Override
  public List<ResourceBundle> getResourceBundles( Pattern keyRegex, Locale locale ) {
    List<ResourceBundle> result = new ArrayList<>();

    for ( Map<String, OSGIResourceBundle> bundleFactoryMap : this.configMap.values() ) {
      for ( OSGIResourceBundle resourceBundle : bundleFactoryMap.values() ) {
        String defaultName = resourceBundle.getDefaultName();

        boolean matchesRegex = keyRegex.matcher( defaultName ).matches();
        if ( matchesRegex ) {
          result.add( resourceBundle );
        }
      }
    }

    return !result.isEmpty() ? result : null;
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
    Map<String, OSGIResourceBundle> newConfigEntry = new HashMap<>();

    final String i18nResourcePattern = "*" + OSGIResourceNamingConvention.RESOURCES_DEFAULT_EXTENSION + "*";
    Enumeration<URL> urlEnumeration = bundle.findEntries( root, i18nResourcePattern, true );
    if ( urlEnumeration != null ) {
      while ( urlEnumeration.hasMoreElements() ) {
        URL url = urlEnumeration.nextElement();
        if ( url != null ) {
          String filename = url.getFile();
          String key = getResourceKey( filename );

          // TODO what do we do about priority http://jira.pentaho.com/browse/BACKLOG-8306
          // int priority = OSGIResourceNamingConvention.getPropertyPriority( filename );
          String defaultName = getResourceDefaultName( filename );
          Locale locale = getResourceLocale( filename );
          newConfigEntry.put( key, getOSGIResourceBundle( key, getResourceUrls( bundle, defaultName, locale ) ) );
        }
      }
    }

    if ( !newConfigEntry.isEmpty() ) {
      synchronized ( this.configMap ) {
        this.configMap.put( bundle.getBundleId(), newConfigEntry );
      }
    }
  }

  private OSGIResourceBundle getOSGIResourceBundle( String resourceName, List<URL> resourceUrls ) {
    OSGIResourceBundle resource = null;

    // load default resource, then go for locale specific
    for ( URL resourceUrl : resourceUrls ) {
      try {
        resource = new OSGIResourceBundle( resourceName, resource, resourceUrl );
      } catch ( IOException e ) {
        // ...
      }
    }

    return resource;
  }

  private List<URL> getResourceUrls( Class clazz, String resourceName, Locale locale ) {
    List<URL> resourceUrls = new ArrayList<>();

    // load default resource, then go for locale specific
    for ( String candidate : OSGIResourceNamingConvention.getCandidateNames( resourceName, locale ) ) {
      URL url = clazz.getResource( candidate + ".properties" );
      if ( url != null ) {
        resourceUrls.add( url );
      }
    }

    return resourceUrls;
  }

  private List<URL> getResourceUrls( Bundle bundle, String resourceName, Locale locale ) {
    List<URL> resourceUrls = new ArrayList<>();

    // load default resource, then go for locale specific
    for ( String candidate : OSGIResourceNamingConvention.getCandidateNames( resourceName, locale ) ) {
      URL url = bundle.getResource( candidate + ".properties" );
      if ( url != null ) {
        resourceUrls.add( url );
      }
    }

    return resourceUrls;
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
