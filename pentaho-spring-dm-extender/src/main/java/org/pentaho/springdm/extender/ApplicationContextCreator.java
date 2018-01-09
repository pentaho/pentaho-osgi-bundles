/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
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
