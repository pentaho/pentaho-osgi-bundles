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

import org.pentaho.osgi.messagewriter.OutputterMessageBodyWriter;
import org.pentaho.osgi.messagewriter.UseOutputter;
import org.pentaho.osgi.messagewriter.builder.OutputBuilderFactory;
import org.pentaho.osgi.messagewriter.builder.Outputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by bryan on 9/5/14.
 */
@Provider
@Produces( MediaType.WILDCARD )
public class OutputterMessageBodyWriterImpl implements OutputterMessageBodyWriter {
  private static final Logger log = LoggerFactory.getLogger( OutputterMessageBodyWriterImpl.class );
  private List<OutputBuilderFactory> outputBuilderFactories;

  public void setOutputBuilderFactories( List<OutputBuilderFactory> outputBuilderFactories ) {
    this.outputBuilderFactories = outputBuilderFactories;
  }

  @Override public boolean isWriteable( Class<?> type, Type genericType, Annotation[] annotations,
                                        MediaType mediaType ) {
    if ( annotations != null ) {
      for ( Annotation annotation : annotations ) {
        if ( UseOutputter.class.equals( annotation.annotationType() ) ) {
          for ( OutputBuilderFactory outputBuilderFactory : outputBuilderFactories ) {
            if ( mediaType
              .equals( new MediaType( outputBuilderFactory.mediaType(), outputBuilderFactory.subType() ) ) ) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  @Override public long getSize( Object object, Class<?> type, Type genericType,
                                 Annotation[] annotations, MediaType mediaType ) {
    return -1;
  }

  @Override public void writeTo( final Object object, Class<?> type, Type genericType,
                                 Annotation[] annotations, final MediaType mediaType,
                                 MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream )
    throws IOException, WebApplicationException {
    OutputBuilderFactory factoryToUse = null;
    for ( OutputBuilderFactory outputterFactory : outputBuilderFactories ) {
      if ( mediaType.equals( new MediaType( outputterFactory.mediaType(), outputterFactory.subType() ) ) ) {
        factoryToUse = outputterFactory;
        break;
      }
    }
    if ( factoryToUse != null ) {
      Outputter outputter = new Outputter( factoryToUse );
      try {
        outputter.set( object );
      } catch ( Exception e ) {
        throw new WebApplicationException( e );
      }
      outputter.write( entityStream );
      return;
    }
    throw new WebApplicationException( new Response() {
      @Override public Object getEntity() {
        return "Unable to find outputter for " + object.getClass() + " with content type " + mediaType;
      }

      @Override public int getStatus() {
        return 500;
      }

      @Override public MultivaluedMap<String, Object> getMetadata() {
        return null;
      }
    } );
  }
}
