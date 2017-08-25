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
package org.pentaho.osgi.metastore.locator.api.impl;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.osgi.api.MetastoreLocatorOsgi;
import org.pentaho.di.job.JobExecutionExtension;
import org.pentaho.di.trans.Trans;

/**
 * Created by tkafalas on 7/10/2017.
 */

@ExtensionPoint( id = "MetastoreLocatorMetaLoadExtensionPoint", extensionPointId = "TransformationMetaLoaded",
  description = "" )
public class MetastoreLocatorExtensionPoint implements ExtensionPointInterface {
  MetastoreLocatorOsgi metastoreLocatorOsgi;

  public MetastoreLocatorExtensionPoint( MetastoreLocatorOsgi metastoreLocatorOsgi ) {
    this.metastoreLocatorOsgi = metastoreLocatorOsgi;
  }

  @Override public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
    AbstractMeta meta;
    if ( object instanceof Trans ) {
      meta = ( (Trans) object ).getTransMeta();
    } else if ( object instanceof JobExecutionExtension ) {
      meta = ( (JobExecutionExtension) object ).job.getJobMeta();
    } else {
      meta = (AbstractMeta) object;
    }
    if ( meta.getMetastoreLocatorOsgi() == null ) {
      meta.setMetastoreLocatorOsgi( metastoreLocatorOsgi );
    }
  }
}
