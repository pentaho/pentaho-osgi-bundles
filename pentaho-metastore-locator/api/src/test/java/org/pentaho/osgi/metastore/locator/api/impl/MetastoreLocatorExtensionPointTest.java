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

import org.junit.Test;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.osgi.api.MetastoreLocatorOsgi;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by tkafalas on 7/25/2017.
 */
public class MetastoreLocatorExtensionPointTest {

  @Test
  public void testCallExtensionPointWithTransMeta() throws Exception {
    MetastoreLocatorOsgi mockMetastoreLocator = mock( MetastoreLocatorOsgi.class );
    LogChannelInterface logChannelInterface = mock( LogChannelInterface.class );
    TransMeta mockTransMeta = mock( TransMeta.class );
    MetastoreLocatorExtensionPoint metastoreLocatorExtensionPoint =
      new MetastoreLocatorExtensionPoint( mockMetastoreLocator );

    metastoreLocatorExtensionPoint.callExtensionPoint( logChannelInterface, mockTransMeta );
    verify( mockTransMeta ).setMetastoreLocatorOsgi( eq( mockMetastoreLocator ) );
  }

  @Test
  public void testCallExtensionPointWithTrans() throws Exception {
    MetastoreLocatorOsgi mockMetastoreLocator = mock( MetastoreLocatorOsgi.class );
    LogChannelInterface logChannelInterface = mock( LogChannelInterface.class );
    TransMeta mockTransMeta = mock( TransMeta.class );
    Trans mockTrans = mock( Trans.class );
    when( mockTrans.getTransMeta() ).thenReturn( mockTransMeta );
    MetastoreLocatorExtensionPoint metastoreLocatorExtensionPoint =
      new MetastoreLocatorExtensionPoint( mockMetastoreLocator );

    metastoreLocatorExtensionPoint.callExtensionPoint( logChannelInterface, mockTrans );
    verify( mockTransMeta ).setMetastoreLocatorOsgi( eq( mockMetastoreLocator ) );
  }

}
