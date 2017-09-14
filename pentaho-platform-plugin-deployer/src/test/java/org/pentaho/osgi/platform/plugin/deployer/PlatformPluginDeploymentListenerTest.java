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

package org.pentaho.osgi.platform.plugin.deployer;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Matchers.eq;
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
