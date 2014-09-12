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

package org.pentaho.osgi.messagewriter.impl;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.osgi.messagewriter.UseOutputter;
import org.pentaho.osgi.messagewriter.builder.MapOutputBuilder;
import org.pentaho.osgi.messagewriter.builder.OutputBuilderFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 9/11/14.
 */
public class OutputterMessageBodyWriterImplTest {
  private List<OutputBuilderFactory> outputBuilderFactories;
  private OutputterMessageBodyWriterImpl outputterMessageBodyWriter;

  @Before
  public void setup() {
    outputBuilderFactories = new ArrayList<OutputBuilderFactory>();
    outputterMessageBodyWriter = new OutputterMessageBodyWriterImpl();
    outputterMessageBodyWriter.setOutputBuilderFactories( outputBuilderFactories );
  }

  @Test
  public void testIsWriteableNullAnnotations() {
    assertFalse( outputterMessageBodyWriter.isWriteable( null, null, null, null ) );
  }

  @Test
  public void testIsWriteableNoUseOutputter() {
    assertFalse( outputterMessageBodyWriter.isWriteable( null, null, new Annotation[] { new Annotation() {
      @Override public Class<? extends Annotation> annotationType() {
        return Override.class;
      }
    } }, null ) );
  }

  @Test
  public void testIsWriteableNoFactories() {
    assertFalse( outputterMessageBodyWriter.isWriteable( null, null, new Annotation[] { new Annotation() {
      @Override public Class<? extends Annotation> annotationType() {
        return UseOutputter.class;
      }
    } }, null ) );
  }

  @Test
  public void testIsWriteableWrongMediaType() {
    OutputBuilderFactory outputBuilderFactory = mock( OutputBuilderFactory.class );
    when( outputBuilderFactory.mediaType() ).thenReturn( "application" );
    when( outputBuilderFactory.subType() ).thenReturn( "xml" );
    outputBuilderFactories.add( outputBuilderFactory );
    assertFalse( outputterMessageBodyWriter.isWriteable( null, null, new Annotation[] { new Annotation() {
      @Override public Class<? extends Annotation> annotationType() {
        return UseOutputter.class;
      }
    } }, MediaType.APPLICATION_JSON_TYPE ) );
  }

  @Test
  public void testIsWriteableTrue() {
    OutputBuilderFactory outputBuilderFactory = mock( OutputBuilderFactory.class );
    when( outputBuilderFactory.mediaType() ).thenReturn( "application" );
    when( outputBuilderFactory.subType() ).thenReturn( "json" );
    outputBuilderFactories.add( outputBuilderFactory );
    assertTrue( outputterMessageBodyWriter.isWriteable( null, null, new Annotation[] { new Annotation() {
      @Override public Class<? extends Annotation> annotationType() {
        return UseOutputter.class;
      }
    } }, MediaType.APPLICATION_JSON_TYPE ) );
  }

  @Test
  public void testGetSize() {
    assertEquals( -1, outputterMessageBodyWriter.getSize( null, null, null, null, null ) );
  }

  @Test( expected = WebApplicationException.class )
  public void testWriteToNoFactory() throws IOException {
    outputterMessageBodyWriter.writeTo( new Object(), null, null, new Annotation[] { new Annotation() {
      @Override public Class<? extends Annotation> annotationType() {
        return UseOutputter.class;
      }
    } }, MediaType.APPLICATION_JSON_TYPE, null, null );
  }

  @Test( expected = WebApplicationException.class )
  public void testWriteToWrongFactory() throws IOException {
    OutputBuilderFactory outputBuilderFactory = mock( OutputBuilderFactory.class );
    when( outputBuilderFactory.mediaType() ).thenReturn( "application" );
    when( outputBuilderFactory.subType() ).thenReturn( "xml" );
    outputBuilderFactories.add( outputBuilderFactory );
    outputterMessageBodyWriter.writeTo( new Object(), null, null, new Annotation[] { new Annotation() {
      @Override public Class<? extends Annotation> annotationType() {
        return UseOutputter.class;
      }
    } }, MediaType.APPLICATION_JSON_TYPE, null, null );
  }

  @Test( expected = WebApplicationException.class )
  public void testWriteToSetException() throws IOException {
    OutputBuilderFactory outputBuilderFactory = mock( OutputBuilderFactory.class );
    when( outputBuilderFactory.mediaType() ).thenReturn( "application" );
    when( outputBuilderFactory.subType() ).thenReturn( "json" );
    when( outputBuilderFactory.createMapOutputBuilder() ).thenThrow( new RuntimeException() );
    outputBuilderFactories.add( outputBuilderFactory );
    outputterMessageBodyWriter.writeTo( new Object(), null, null, new Annotation[] { new Annotation() {
      @Override public Class<? extends Annotation> annotationType() {
        return UseOutputter.class;
      }
    } }, MediaType.APPLICATION_JSON_TYPE, null, null );
  }

  @Test( expected = WebApplicationException.class )
  public void testThrown() throws IOException {
    OutputBuilderFactory outputBuilderFactory = mock( OutputBuilderFactory.class );
    when( outputBuilderFactory.mediaType() ).thenReturn( "application" );
    when( outputBuilderFactory.subType() ).thenReturn( "xml" );
    outputBuilderFactories.add( outputBuilderFactory );
    try {
      outputterMessageBodyWriter.writeTo( new Object(), null, null, new Annotation[] { new Annotation() {
        @Override public Class<? extends Annotation> annotationType() {
          return UseOutputter.class;
        }
      } }, MediaType.APPLICATION_JSON_TYPE, null, null );
    } catch ( WebApplicationException e ) {
      assertTrue( e.getResponse().getEntity().toString().startsWith( "Unable to find outputter for " ) );
      assertEquals( 500, e.getResponse().getStatus() );
      assertNull( e.getResponse().getMetadata() );
      throw e;
    }
  }

  @Test( expected = IOException.class )
  public void testWriteException() throws IOException {
    OutputStream outputStream = mock( OutputStream.class );
    OutputBuilderFactory outputBuilderFactory = mock( OutputBuilderFactory.class );
    MapOutputBuilder mapOutputBuilder = mock( MapOutputBuilder.class );
    when( outputBuilderFactory.mediaType() ).thenReturn( "application" );
    when( outputBuilderFactory.subType() ).thenReturn( "json" );
    when( outputBuilderFactory.createMapOutputBuilder() ).thenReturn( mapOutputBuilder );
    doThrow( new IOException() ).when( mapOutputBuilder ).write( outputStream );
    outputBuilderFactories.add( outputBuilderFactory );
    outputterMessageBodyWriter.writeTo( new Object(), null, null, new Annotation[] { new Annotation() {
      @Override public Class<? extends Annotation> annotationType() {
        return UseOutputter.class;
      }
    } }, MediaType.APPLICATION_JSON_TYPE, null, outputStream );
  }


  @Test
  public void testWriteTo() throws IOException {
    OutputStream outputStream = mock( OutputStream.class );
    OutputBuilderFactory outputBuilderFactory = mock( OutputBuilderFactory.class );
    when( outputBuilderFactory.mediaType() ).thenReturn( "application" );
    when( outputBuilderFactory.subType() ).thenReturn( "json" );
    MapOutputBuilder mapOutputBuilder = mock( MapOutputBuilder.class );
    when( outputBuilderFactory.createMapOutputBuilder() ).thenReturn( mapOutputBuilder );
    outputBuilderFactories.add( outputBuilderFactory );
    outputterMessageBodyWriter.writeTo( new Object(), null, null, new Annotation[] { new Annotation() {
      @Override public Class<? extends Annotation> annotationType() {
        return UseOutputter.class;
      }
    } }, MediaType.APPLICATION_JSON_TYPE, null, outputStream );
  }
}
