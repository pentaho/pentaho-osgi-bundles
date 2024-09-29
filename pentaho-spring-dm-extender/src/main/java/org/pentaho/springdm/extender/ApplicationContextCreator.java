/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
