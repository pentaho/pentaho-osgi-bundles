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
 * Copyright 2015 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.osgi.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.pentaho.osgi.api.BeanFactoryLocator;
import org.pentaho.osgi.api.ProxyUnwrapper;
import org.pentaho.osgi.api.IKarafFeatureWatcher;

/**
 * User: nbaker Date: 12/17/10
 */
public class BeanFactoryActivator implements BundleActivator {

  @Override
  public void start( BundleContext bundleContext ) throws Exception {
    bundleContext.registerService( ProxyUnwrapper.class.getName(), new ProxyUnwrapperImpl( bundleContext ), null );
    bundleContext.registerService( BeanFactoryLocator.class.getName(), new BeanFactoryLocatorImpl(), null );
    ServiceReference ref = bundleContext.getServiceReference( ConfigurationAdmin.class.getName() );
    ConfigurationAdmin admin = (ConfigurationAdmin) bundleContext.getService( ref );

    KarafFeatureWatcherImpl karafFeatureWatcher = new KarafFeatureWatcherImpl( bundleContext );
    bundleContext.registerService( IKarafFeatureWatcher.class.getName(), karafFeatureWatcher, null );

  }

  @Override
  public void stop( BundleContext bundleContext ) throws Exception {

  }
}
  