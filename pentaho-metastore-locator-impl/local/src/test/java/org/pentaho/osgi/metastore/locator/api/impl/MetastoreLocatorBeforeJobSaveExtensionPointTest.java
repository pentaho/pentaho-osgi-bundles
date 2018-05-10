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
