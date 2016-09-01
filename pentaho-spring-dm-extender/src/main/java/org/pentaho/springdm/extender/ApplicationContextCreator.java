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

package org.pentaho.springdm.extender;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.context.DelegatedExecutionOsgiBundleApplicationContext;
import org.springframework.osgi.extender.OsgiApplicationContextCreator;
import org.springframework.osgi.extender.support.ApplicationContextConfiguration;
import org.springframework.osgi.extender.support.scanning.ConfigurationScanner;
import org.springframework.osgi.extender.support.scanning.DefaultConfigurationScanner;
import org.springframework.osgi.util.OsgiStringUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Created by nbaker on 7/20/16.
 */
public class ApplicationContextCreator implements OsgiApplicationContextCreator {

  private static final Logger log;
  private ConfigurationScanner configurationScanner = new DefaultConfigurationScanner();

  public ApplicationContextCreator() {
  }

  public DelegatedExecutionOsgiBundleApplicationContext createApplicationContext( BundleContext bundleContext )
      throws Exception {
    Bundle bundle = bundleContext.getBundle();
    ApplicationContextConfiguration config = new ApplicationContextConfiguration( bundle, this.configurationScanner );
    if ( log.isTraceEnabled() ) {
      log.trace(
          "Created configuration " + config + " for bundle " + OsgiStringUtils.nullSafeNameAndSymName( bundle ) );
    }

    if ( !config.isSpringPoweredBundle() ) {
      return null;
    } else {
      log.info( "Discovered configurations " + ObjectUtils.nullSafeToString( config.getConfigurationLocations() )
          + " in bundle [" + OsgiStringUtils.nullSafeNameAndSymName( bundle ) + "]" );
      PentahoOsgiBundleXmlApplicationContext sdoac = new PentahoOsgiBundleXmlApplicationContext( config.getConfigurationLocations() );
      sdoac.setBundleContext( bundleContext );
      sdoac.setPublishContextAsService( config.isPublishContextAsService() );

      return sdoac;
    }
  }

  public void setConfigurationScanner( ConfigurationScanner configurationScanner ) {
    Assert.notNull( configurationScanner );
    this.configurationScanner = configurationScanner;
  }

  static {
    log = LoggerFactory.getLogger( ApplicationContextCreator.class );
  }
}
