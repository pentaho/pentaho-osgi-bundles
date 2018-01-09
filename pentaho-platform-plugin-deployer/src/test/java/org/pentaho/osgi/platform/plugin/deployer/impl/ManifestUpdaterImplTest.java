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
