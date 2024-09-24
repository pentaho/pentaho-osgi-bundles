/*!
 * Copyright 2018 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.webpackage.extender.http.impl;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.pentaho.webpackage.core.IPentahoWebPackage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PentahoWebPackageResourceMappingTest {

  private PentahoWebPackageResourceMapping pentahoWebPackageResourceMapping;
  private IPentahoWebPackage mockPentahoWebPackage;
  private String resourcesRootPath = "/some/path/to/resources/root";
  private String webRootPath = "/some/path/to/web/root";

  @Before
  public void setUp() {
    mockPentahoWebPackage = mock( IPentahoWebPackage.class );
    when( mockPentahoWebPackage.getResourceRootPath() ).thenReturn( resourcesRootPath );
    when( mockPentahoWebPackage.getWebRootPath() ).thenReturn( webRootPath );
  }

  @Test
  public void testGetAliasShouldReturnValidWebRootPath() {
    // arrange
    this.pentahoWebPackageResourceMapping =
        new PentahoWebPackageResourceMapping( this.mockPentahoWebPackage );
    String expectedWebRootPath = mockPentahoWebPackage.getWebRootPath();

    // act
    String actualWebRootPath = this.pentahoWebPackageResourceMapping.getAlias();

    // assert
    assertEquals( "Should return valid Webroot Path", expectedWebRootPath, actualWebRootPath );
  }

  @Test
  public void testGetPathShouldReturnValidResourcesRootPath() {
    // arrange
    this.pentahoWebPackageResourceMapping =
        new PentahoWebPackageResourceMapping( this.mockPentahoWebPackage );
    String expectedResourcesRootPath = this.mockPentahoWebPackage.getResourceRootPath();

    // act
    String actualResourcesRootPath = this.pentahoWebPackageResourceMapping.getPath();

    // assert
    assertEquals( "Should return valid Resources Root Path", expectedResourcesRootPath, actualResourcesRootPath );
  }

  @Test
  public void testToStringShouldReturnValidToString() {
    // arrange
    this.pentahoWebPackageResourceMapping =
        new PentahoWebPackageResourceMapping( this.mockPentahoWebPackage );
    String expectedAlias = mockPentahoWebPackage.getWebRootPath();
    String expectedPath = mockPentahoWebPackage.getResourceRootPath();
    String expectedToString = PentahoWebPackageResourceMapping.class.getSimpleName()
        + "{" + "alias=" + expectedAlias + ",path=" + expectedPath + "}";

    // act
    String actualToString = this.pentahoWebPackageResourceMapping.toString();

    // assert
    assertEquals( "Should Return Valid To String", expectedToString, actualToString );
  }

  @Test
  public void testEqualsWhereObjectNull() {
    // arrange
    this.pentahoWebPackageResourceMapping =
        new PentahoWebPackageResourceMapping( null /* not relevant */ );

    // assert
    assertFalse( this.pentahoWebPackageResourceMapping.equals( null ) );
  }

  @Test
  public void testEqualsWhereObjectIsInvalidReference() {
    // arrange
    this.pentahoWebPackageResourceMapping =
        new PentahoWebPackageResourceMapping( null /* not relevant */ );

    // act
    Object invalidRef = new Object();

    // assert
    assertFalse( this.pentahoWebPackageResourceMapping.equals( invalidRef ) );
  }

  @Test
  public void testEqualsWhereObjectIsSameReference() {
    // arrange
    this.pentahoWebPackageResourceMapping =
        new PentahoWebPackageResourceMapping( this.mockPentahoWebPackage );

    // act
    Object sameRef = this.pentahoWebPackageResourceMapping;

    // assert
    assertTrue( this.pentahoWebPackageResourceMapping.equals( sameRef ) );
  }

  @Test
  public void testEqualsWhereObjectIsValidReferenceOther() {
    // arrange
    this.pentahoWebPackageResourceMapping =
        new PentahoWebPackageResourceMapping( this.mockPentahoWebPackage );

    // act
    Object other =
        new PentahoWebPackageResourceMapping( this.mockPentahoWebPackage );

    // assert
    assertTrue( this.pentahoWebPackageResourceMapping.equals( other ) );
  }
}