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
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobExecutionExtension;
import org.pentaho.di.job.JobMeta;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by tkafalas on 8/11/2017.
 */
public class MetastoreLocatorBeforeJobSaveExtensionPointTest {

  @Test
  public void testCallExtensionPoint() throws Exception {
    MetastoreLocatorOsgi mockMetastoreLocator = mock( MetastoreLocatorOsgi.class );
    LogChannelInterface logChannelInterface = mock( LogChannelInterface.class );
    JobExecutionExtension mockJobExecutionExtension = mock( JobExecutionExtension.class );
    Job mockJob = mock( Job.class );
    JobMeta mockJobMeta = mock( JobMeta.class );
    mockJobExecutionExtension.job = mockJob;
    when( mockJob.getJobMeta() ).thenReturn( mockJobMeta );
    MetastoreLocatorBeforeJobSaveExtensionPoint metastoreLocatorBeforeJobSaveExtensionPoint =
      new MetastoreLocatorBeforeJobSaveExtensionPoint( mockMetastoreLocator );

    metastoreLocatorBeforeJobSaveExtensionPoint.callExtensionPoint( logChannelInterface, mockJobExecutionExtension );
    verify( mockJobMeta ).setMetastoreLocatorOsgi( eq( mockMetastoreLocator ) );
  }

}
