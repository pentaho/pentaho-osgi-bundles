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
