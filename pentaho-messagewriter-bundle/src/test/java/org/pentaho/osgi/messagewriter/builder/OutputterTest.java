/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.osgi.messagewriter.builder;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.osgi.messagewriter.OutputException;
import org.pentaho.osgi.messagewriter.Outputtable;
import org.pentaho.osgi.messagewriter.PrimitiveOutputBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 9/11/14.
 */
public class OutputterTest {
  private OutputBuilderFactory outputBuilderFactory;
  private Outputter outputter;

  @Before
  public void setup() {
    outputBuilderFactory = mock( OutputBuilderFactory.class );
    outputter = new Outputter( outputBuilderFactory );
  }

  @Test
  public void testOutputBuilderFactoryConstructor() {
    assertEquals( outputBuilderFactory, outputter.getOutputBuilderFactory() );
  }

  @Test
  public void testGetObjectMethods() throws NoSuchMethodException {
    assertTrue( Outputter.getObjectMethods().size() > 0 );
    for ( String method : Outputter.getObjectMethods() ) {
      Object.class.getMethod( method );
    }
  }

  @Test
  public void testSetOutputtable() throws OutputException, IOException {
    OutputStream outputStream = mock( OutputStream.class );
    MapOutputBuilder mapOutputBuilder = mock( MapOutputBuilder.class );
    when( outputBuilderFactory.createMapOutputBuilder() ).thenReturn( mapOutputBuilder );
    outputter.set( new TestOutputtable() );
    outputter.write( outputStream );
    verify( mapOutputBuilder ).put( eq( "id" ), any( TestReference.class ) );
    verify( mapOutputBuilder ).write( outputStream );
  }

  @Test
  public void testSetMap() throws OutputException, IOException {
    OutputStream outputStream = mock( OutputStream.class );
    MapOutputBuilder mapOutputBuilder = mock( MapOutputBuilder.class );
    when( outputBuilderFactory.createMapOutputBuilder() ).thenReturn( mapOutputBuilder );
    Map<String, Object> map = new HashMap<String, Object>();
    map.put( "test-key", new TestOutputtable() );
    outputter.set( map );
    outputter.write( outputStream );
    verify( mapOutputBuilder ).put( eq( "test-key" ), any( TestOutputtable.class ) );
    verify( mapOutputBuilder ).write( outputStream );
  }

  @Test
  public void testSetList() throws OutputException, IOException {
    OutputStream outputStream = mock( OutputStream.class );
    ListOutputBuilder listOutputBuilder = mock( ListOutputBuilder.class );
    when( outputBuilderFactory.createListOutputBuilder() ).thenReturn( listOutputBuilder );
    outputter.set( Arrays.asList( new TestOutputtable() ) );
    outputter.write( outputStream );
    verify( listOutputBuilder ).add( any( TestOutputtable.class ) );
    verify( listOutputBuilder ).write( outputStream );
  }

  @Test
  public void testSetPrimitive() throws OutputException, IOException {
    OutputStream outputStream = mock( OutputStream.class );
    PrimitiveOutputBuilder primitiveOutputBuilder = mock( PrimitiveOutputBuilder.class );
    when( outputBuilderFactory.createPrimitiveOutputBuilder() ).thenReturn( primitiveOutputBuilder );
    outputter.set( 2 );
    outputter.write( outputStream );
    verify( primitiveOutputBuilder ).set( 2 );
    verify( primitiveOutputBuilder ).write( outputStream );
  }

  @Test
  public void testSetNull() throws OutputException, IOException {
    OutputStream outputStream = mock( OutputStream.class );
    PrimitiveOutputBuilder primitiveOutputBuilder = mock( PrimitiveOutputBuilder.class );
    when( outputBuilderFactory.createPrimitiveOutputBuilder() ).thenReturn( primitiveOutputBuilder );
    outputter.set( null );
    outputter.write( outputStream );
    verify( primitiveOutputBuilder ).set( null );
    verify( primitiveOutputBuilder ).write( outputStream );
  }

  @Test
  public void testSetObject() throws OutputException, IOException {
    OutputStream outputStream = mock( OutputStream.class );
    MapOutputBuilder mapOutputBuilder = mock( MapOutputBuilder.class );
    when( outputBuilderFactory.createMapOutputBuilder() ).thenReturn( mapOutputBuilder );
    outputter.set( new TestReference() );
    outputter.write( outputStream );
    verify( mapOutputBuilder ).put( "name", TestReference.name );
    verify( mapOutputBuilder ).put( "n", TestReference.name2 );
    verify( mapOutputBuilder ).write( outputStream );
    verifyNoMoreInteractions( mapOutputBuilder );
  }

  @Test( expected = OutputException.class )
  public void testSetObjectInvocationTargetException() throws OutputException, IOException {
    OutputStream outputStream = mock( OutputStream.class );
    MapOutputBuilder mapOutputBuilder = mock( MapOutputBuilder.class );
    when( outputBuilderFactory.createMapOutputBuilder() ).thenReturn( mapOutputBuilder );
    TestReference testReference = mock( TestReference.class );
    when( testReference.getName() ).thenThrow( new RuntimeException() );
    outputter.set( testReference );
    outputter.write( outputStream );
  }

  private class TestOutputtable implements Outputtable {

    @Override public OutputBuilder output( OutputBuilderFactory outputBuilderFactory ) throws OutputException {
      MapOutputBuilder mapOutputBuilder = outputBuilderFactory.createMapOutputBuilder();
      mapOutputBuilder.put( "id", new TestReference() );
      return mapOutputBuilder;
    }
  }

  private class TestReference {
    public static final String name = "test-name";
    public static final String name2 = "test-name2";

    public String getName() {
      return name;
    }

    public String e() {
      return name;
    }

    public String getNameFake( String name ) {
      return name;
    }

    public String getN() {
      return name2;
    }
  }
}
