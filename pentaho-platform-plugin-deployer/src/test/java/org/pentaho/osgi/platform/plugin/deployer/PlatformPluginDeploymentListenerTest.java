/*!
 * Copyright 2010 - 2024 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.osgi.platform.plugin.deployer;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 8/26/14.
 */
public class PlatformPluginDeploymentListenerTest {
  private PlatformPluginDeploymentListener.URLFactory urlFactory;
  private PlatformPluginDeploymentListener platformPluginDeploymentListener;

  @Before
  public void setup() {
    urlFactory = mock( PlatformPluginDeploymentListener.URLFactory.class );
    platformPluginDeploymentListener = new PlatformPluginDeploymentListener();
    platformPluginDeploymentListener.setUrlFactory( urlFactory );
  }
  @Test
  public void testCantHandleNullFile() {
    assertFalse( platformPluginDeploymentListener.canHandle( null ) );
  }

  @Test
  public void testCantHandleNullFileName() {
    File file = mock( File.class );
    assertFalse( platformPluginDeploymentListener.canHandle( file ) );
  }

  @Test
  public void testCantHandleNonZip() {
    File file = mock( File.class );
    when( file.getName() ).thenReturn( "Test.notzip" );
    assertFalse( platformPluginDeploymentListener.canHandle( file ) );
  }

  @Test
  public void testEatsExceptionForNonexistentFile() {
    platformPluginDeploymentListener.canHandle( new File( "THIS_FILE_SHOULD_NOT_EXIST_ON_YOUR_MACHINE_IT_IS_FOR_TESTING_A_NEGATIVE_CASE.dontcreatethisfile.zip" ) );
  }

  @Test
  public void testCanHandleWithPluginXmlOneDirDown() {
    File file = new File( this.getClass().getClassLoader()
      .getResource( "org/pentaho/osgi/platform/plugin/deployer/testCanHandleWithPluginXmlOneDirDown.zip" ).getFile() );
    assertTrue( platformPluginDeploymentListener.canHandle( file ) );
  }

  @Test
  public void testCantHandleNoPluginXml() {
    File file = new File( this.getClass().getClassLoader()
      .getResource( "org/pentaho/osgi/platform/plugin/deployer/testCantHandleNoPlugin.xml.zip" ).getFile() );
    assertFalse( platformPluginDeploymentListener.canHandle( file ) );
  }

  @Test
  public void testTransform() throws Exception {
    platformPluginDeploymentListener.transform( this.getClass().getClassLoader()
      .getResource( "org/pentaho/osgi/platform/plugin/deployer/testTranform.zip" ) );
    verify( urlFactory ).create( eq( PlatformPluginDeploymentListener.PROTOCOL ), endsWith( "testTranform.zip" ) );
  }
}
