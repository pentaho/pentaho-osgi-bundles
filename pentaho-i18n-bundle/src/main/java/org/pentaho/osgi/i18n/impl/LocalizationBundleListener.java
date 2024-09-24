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
