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
package org.pentaho.osgi.i18n.impl;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bryan on 9/4/14.
 */
public class LocalizationBundleListener implements BundleListener {
  private static Logger log = LoggerFactory.getLogger( LocalizationBundleListener.class );
  private LocalizationManager localizationManager;
  private BundleContext bundleContext;

  public void setLocalizationManager( LocalizationManager localizationManager ) {
    this.localizationManager = localizationManager;
  }

  public void setBundleContext( BundleContext bundleContext ) {
    this.bundleContext = bundleContext;
  }

  // For unit test only
  static void setLog( Logger log ) {
    LocalizationBundleListener.log = log;
  }

  // For unit test only
  static Logger getLog() {
    return log;
  }

  @Override public void bundleChanged( BundleEvent event ) {
    switch ( event.getType() ) {
      case BundleEvent.STARTED:
      case BundleEvent.STOPPED:
        try {
          localizationManager.bundleChanged( event.getBundle() );
        } catch ( Exception e ) {
          log.error( e.getMessage(), e );
        }
        break;
    }
  }

  public void init() throws Exception {
    bundleContext.addBundleListener( this );
    for ( Bundle bundle : bundleContext.getBundles() ) {
      if ( bundle.getState() == Bundle.ACTIVE ) {
        bundleChanged( new BundleEvent( BundleEvent.STARTED, bundle ) );
      }
    }
  }
}
