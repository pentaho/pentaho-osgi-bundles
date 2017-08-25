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

package org.pentaho.osgi.platform.plugin.deployer.impl;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.jar.Manifest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by bryan on 8/27/14.
 */
public class ManifestUpdaterImplTest {
  @Test
  public void testConstructor() {
    ManifestUpdaterImpl manifest = new ManifestUpdaterImpl();
    assertNotNull( manifest.getExportServices() );
    assertNotNull( manifest.getImports() );
  }

  @Test
  public void testJoinMultiple() {
    ManifestUpdaterImpl manifest = new ManifestUpdaterImpl();
    assertEquals( "test..1..2", manifest.join( Arrays.asList( "test", "1", "2" ), ".." ) );
  }

  @Test
  public void testJoinNone() {
    ManifestUpdaterImpl manifest = new ManifestUpdaterImpl();
    assertEquals( "", manifest.join( Arrays.<String>asList(), ".." ) );
  }

  @Test
  public void testGetImportString() {
    ManifestUpdaterImpl manifestUpdater = new ManifestUpdaterImpl();
    manifestUpdater.getImports().put( "test1", null );
    manifestUpdater.getImports().put( "test2", "abc" );
    assertEquals( "test1,test2;version=\"abc\"", manifestUpdater.getImportString() );
  }

  @Test
  public void testWriteNoOriginalManifest() throws IOException {
    ManifestUpdaterImpl manifestUpdater = new ManifestUpdaterImpl();
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( );
    manifestUpdater.write( null, byteArrayOutputStream, "test", "test.symbolic", "version" );
    Manifest manifest = new Manifest( new ByteArrayInputStream( byteArrayOutputStream.toByteArray() ) );
    assertEquals( "test.symbolic", manifest.getMainAttributes().getValue( "Bundle-SymbolicName" ) );
    assertEquals( "test", manifest.getMainAttributes().getValue( "Bundle-Name" ) );
    assertEquals( "version", manifest.getMainAttributes().getValue( "Bundle-Version" ) );
  }

  @Test
  public void testWriteOriginalManifest() throws IOException {
    ManifestUpdaterImpl manifestUpdater = new ManifestUpdaterImpl();
    Manifest original = new Manifest( );
    original.getMainAttributes().putValue( "test", "ing" );
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( );
    manifestUpdater.write( original, byteArrayOutputStream, "test", "test.symbolic", "version" );
    Manifest manifest = new Manifest( new ByteArrayInputStream( byteArrayOutputStream.toByteArray() ) );
    assertEquals( "test.symbolic", manifest.getMainAttributes().getValue( "Bundle-SymbolicName" ) );
    assertEquals( "test", manifest.getMainAttributes().getValue( "Bundle-Name" ) );
    assertEquals( "version", manifest.getMainAttributes().getValue( "Bundle-Version" ) );
    assertEquals( "version", manifest.getMainAttributes().getValue( "Bundle-Version" ) );
  }

  @Test
  public void testAddEntries() throws Exception {
    UUID id = UUID.randomUUID();
    ManifestUpdaterImpl manifestUpdater = new ManifestUpdaterImpl();
    manifestUpdater.addEntry( "test", "value" );
    manifestUpdater.addEntry( "company", "pentaho" );
    manifestUpdater.addEntry( "id", id );

    Manifest original = new Manifest( );
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( );
    manifestUpdater.write( original, byteArrayOutputStream, "test", "test.symbolic", "version" );
    Manifest manifest = new Manifest( new ByteArrayInputStream( byteArrayOutputStream.toByteArray() ) );

    assertEquals( "value",  manifest.getMainAttributes().getValue( "test" ) );
    assertEquals( "pentaho",  manifest.getMainAttributes().getValue( "company" ) );
    assertEquals( id.toString(), manifest.getMainAttributes().getValue( "id" ) );
  }

  @Test
  public void testBundleSymbolicName() throws Exception {
    String name = "bundle sym name";
    ManifestUpdaterImpl manifestUpdater = new ManifestUpdaterImpl();
    manifestUpdater.setBundleSymbolicName( name );

    Manifest original = new Manifest( );
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( );
    manifestUpdater.write( original, byteArrayOutputStream, "test", "test.symbolic", "version" );
    Manifest manifest = new Manifest( new ByteArrayInputStream( byteArrayOutputStream.toByteArray() ) );

    assertEquals( name, manifestUpdater.getBundleSymbolicName() );
    assertEquals( name,  manifest.getMainAttributes().getValue( "Bundle-SymbolicName" ) );

  }
}
