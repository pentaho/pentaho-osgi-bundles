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
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
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
