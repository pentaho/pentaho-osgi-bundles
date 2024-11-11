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

package org.pentaho.webpackage.core.impl;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PentahoWebPackageImplTest {

  private String packageName = "SomeName";
  private String packageVersion = "1.2.3";
  private String mockPackageJson = "{\"name\":\"" + packageName + "\",\"version\":\"" + packageVersion + "\"}";

  @Test
  public void testGetNameShouldReturnPackageNameGivenTheUrlInConstructor() {
    // arrange
    String expectedName = this.packageName;
    URL mockUrl = this.createMockUrlConnection( mockPackageJson );
    PentahoWebPackageImpl pentahoWebPackage = new PentahoWebPackageImpl( null, mockUrl );

    // act
    String actualName = pentahoWebPackage.getName();

    //assert
    assertEquals( "Should return Package name", expectedName, actualName );
  }

  @Test
  public void testGetVersionShouldReturnPackageVersionGivenTheUrlInConstructor() {
    // arrange
    String expectedVersion = this.packageVersion;
    URL mockUrl = this.createMockUrlConnection( mockPackageJson );
    PentahoWebPackageImpl pentahoWebPackage = new PentahoWebPackageImpl( null, mockUrl );

    // act
    String actualVersion = pentahoWebPackage.getVersion();

    // assert
    assertEquals( "Should return Package version", expectedVersion, actualVersion );
  }

  @Test
  public void testGetResourceRootPathShouldReturnResourceRootPathGivenInConstructor() {
    // arrange
    String expectedRootPath = "some/resource/path";
    URL mockUrl = this.createMockUrlConnection( mockPackageJson );
    PentahoWebPackageImpl pentahoWebPackage = new PentahoWebPackageImpl( expectedRootPath, mockUrl );

    // act
    String actualRootPath = pentahoWebPackage.getResourceRootPath();

    // assert
    assertEquals( "Should return Package Resource Root Path", expectedRootPath, actualRootPath );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testPentahoWebPackageImplShouldThrowExceptionOnInvalidWebpackageNameOrVersion() {
    // arrange
    String invalidMockPackageJson = "{}";
    URL mockUrl = this.createMockUrlConnection( invalidMockPackageJson );

    // act
    PentahoWebPackageImpl pentahoWebPackage = new PentahoWebPackageImpl( null, mockUrl );
  }

  @Test
  public void testGetWebRootPathShouldReturnValidWebRootPathGivenThePackageJsonOnTheUrl() {
    // arrange
    String name = "SomeName";
    String version = "1.2.3";
    String expectedWebRootPath = "/" + name + "@" + version;
    URL mockUrl = this.createMockUrlConnection( mockPackageJson );
    PentahoWebPackageImpl pentahoWebPackage = new PentahoWebPackageImpl( null, mockUrl );

    // act
    String actualWebRootPath = pentahoWebPackage.getWebRootPath();

    // assert
    assertEquals( "Should return WebPackage web root path", expectedWebRootPath, actualWebRootPath );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testGetPackageJsonShouldThrowExceptionOnInvalidUrlConnection() {
    // arrange
    URL mockUrl = this.createInvalidMockUrlConnection( mockPackageJson );
    PentahoWebPackageImpl pentahoWebPackage = new PentahoWebPackageImpl( null, mockUrl );

    // act
    pentahoWebPackage.getPackageJson();
  }

  private URL createMockUrlConnection( String payload ) {
    URLConnection mockUrlCon = mock( URLConnection.class );
    URLStreamHandler stubUrlHandler = null;
    try {
      stubUrlHandler = new URLStreamHandler() {
        @Override
        protected URLConnection openConnection( URL u ) throws IOException {
          return mockUrlCon;
        }
      };
      when( mockUrlCon.getInputStream() ).thenReturn( new ByteArrayInputStream( payload.getBytes() ) );
    } catch ( IOException ignored ) {
    }
    try {
      return new URL( "http", "someurl.com", 9999, "", stubUrlHandler );
    } catch ( MalformedURLException e ) {
      e.printStackTrace();
    }
    return null;
  }

  private URL createInvalidMockUrlConnection( String payload ) {
    URLStreamHandler stubUrlHandler = stubUrlHandler = new URLStreamHandler() {
      @Override
      protected URLConnection openConnection( URL u ) throws IOException {
        throw new IOException( "Can't open connection" );
      }
    };

    try {
      return new URL( "http", "someurl.com", 9999, "", stubUrlHandler );
    } catch ( MalformedURLException e ) {
      e.printStackTrace();
    }
    return null;
  }
}
