package org.pentaho.webpackage.core.impl;

import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;

public class PentahoWebPackageImplTest {

  private String mockPackageJson = "{\"name\":\"SomeName\",\"description\":\"A packaged foo fooer for fooing foos\",\"main\":\"foo.js\",\"man\":[\".\\/man\\/foo.1\",\".\\/man\\/bar.1\"],\"version\":\"1.2.3\"}";

  @Test
  public void testGetNameShouldReturnPackageNameGivenTheUrlInConstructor() {
    // arrange
    String expectedName = "SomeName";
    URL mockUrl = TestUtils.createMockUrlConnection( mockPackageJson );
    PentahoWebPackageImpl pentahoWebPackage = new PentahoWebPackageImpl( null, mockUrl );

    // act
    String actualName = pentahoWebPackage.getName();

    //assert
    assertEquals( "Should return Package name", expectedName, actualName );
  }

  @Test
  public void testGetVersionShouldReturnPackageVersionGivenTheUrlInConstructor() {
    // arrange
    String expectedVersion = "1.2.3";
    URL mockUrl = TestUtils.createMockUrlConnection( mockPackageJson );
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
    URL mockUrl = TestUtils.createMockUrlConnection( mockPackageJson );
    PentahoWebPackageImpl pentahoWebPackage = new PentahoWebPackageImpl( expectedRootPath, mockUrl );

    // act
    String actualRootPath = pentahoWebPackage.getResourceRootPath();

    // assert
    assertEquals( "Should return Package Resource Root Path", expectedRootPath, actualRootPath );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testPentahoWebPackageImplShouldThrowExceptionOnInvalidWebpackageNameOrVersion() {
    // arrange
    String invalidMockPackageJson = "{\"description\":\"A packaged foo fooer for fooing foos\",\"main\":\"foo.js\",\"man\":[\".\\/man\\/foo.1\",\".\\/man\\/bar.1\"]}";
    URL mockUrl = TestUtils.createMockUrlConnection( invalidMockPackageJson );

    // act
    PentahoWebPackageImpl pentahoWebPackage = new PentahoWebPackageImpl( null, mockUrl );
  }

  @Test
  public void testGetWebRootPathShouldReturnValidWebRootPathGivenThePackageJsonOnTheUrl() {
    // arrange
    String name = "SomeName";
    String version = "1.2.3";
    String expectedWebRootPath = "/" + name + "/" + version;
    URL mockUrl = TestUtils.createMockUrlConnection( mockPackageJson );
    PentahoWebPackageImpl pentahoWebPackage = new PentahoWebPackageImpl( null, mockUrl );

    // act
    String actualWebRootPath = pentahoWebPackage.getWebRootPath();

    // assert
    assertEquals( "Should return WebPackage web root path", expectedWebRootPath, actualWebRootPath );
  }

  /*
      This test is just for coverage
   */
  @Test( expected = IllegalArgumentException.class )
  public void testGetPackageJsonShouldThrowExceptionOnInvalidUrlConnection() {
    // arrange
    URL mockUrl = TestUtils.createInvalidMockUrlConnection(  mockPackageJson );
    PentahoWebPackageImpl pentahoWebPackage = new PentahoWebPackageImpl( null, mockUrl );

    // act
    pentahoWebPackage.getPackageJson();
  }
}
