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

import org.pentaho.osgi.messagewriter.OutputException;
import org.pentaho.osgi.messagewriter.Outputtable;
import org.pentaho.osgi.messagewriter.PrimitiveOutputBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by bryan on 9/11/14.
 */
public class Outputter implements ObjectOutputBuilder {
  private static final Set<String> objectMethods = initObjectMethods();
  private final OutputBuilderFactory outputBuilderFactory;
  private OutputBuilder outputBuilder;

  public Outputter( OutputBuilderFactory outputBuilderFactory ) {
    this.outputBuilderFactory = outputBuilderFactory;
  }

  private static Set<String> initObjectMethods() {
    Set<String> result = new HashSet<String>();
    for ( Method method : Object.class.getMethods() ) {
      String methodName = method.getName();
      Class<?>[] types = method.getParameterTypes();
      if ( methodName.length() > 3 && methodName.startsWith( "get" ) && types.length == 0 ) {
        result.add( methodName );
      }
    }
    return result;
  }

  protected static Set<String> getObjectMethods() {
    return objectMethods;
  }

  protected OutputBuilderFactory getOutputBuilderFactory() {
    return outputBuilderFactory;
  }

  public void set( Object object ) throws OutputException {
    if ( object instanceof Outputtable ) {
      outputBuilder = ( (Outputtable) object ).output( outputBuilderFactory );
    } else if ( object instanceof Map ) {
      MapOutputBuilder mapOutputBuilder = outputBuilderFactory.createMapOutputBuilder();
      for ( Map.Entry<String, ?> objectEntry : ( (Map<String, ?>) object ).entrySet() ) {
        mapOutputBuilder.put( objectEntry.getKey(), objectEntry.getValue() );
      }
      outputBuilder = mapOutputBuilder;
    } else if ( object instanceof Collection ) {
      ListOutputBuilder listOutputBuilder = outputBuilderFactory.createListOutputBuilder();
      for ( Object value : (Collection<?>) object ) {
        listOutputBuilder.add( value );
      }
      outputBuilder = listOutputBuilder;
    } else {
      if ( object == null || PrimitiveOutputBuilder.primitiveClasses.contains( object.getClass() ) ) {
        PrimitiveOutputBuilder primitiveOutputBuilder = outputBuilderFactory.createPrimitiveOutputBuilder();
        primitiveOutputBuilder.set( object );
        outputBuilder = primitiveOutputBuilder;
      } else {
        try {
          outputBuilder = buildMap( object );
        } catch ( Exception e ) {
          throw new OutputException( e );
        }
      }
    }
  }

  private MapOutputBuilder buildMap( Object object )
    throws InvocationTargetException, IllegalAccessException, OutputException {
    MapOutputBuilder mapOutputBuilder = outputBuilderFactory.createMapOutputBuilder();
    for ( Method method : object.getClass().getMethods() ) {
      String methodName = method.getName();
      if ( methodName.length() > 3 && methodName.startsWith( "get" ) && method.getParameterTypes().length == 0
        && !objectMethods.contains( methodName ) ) {
        String attributeName = method.getName().substring( 3, 4 ).toLowerCase();
        if ( methodName.length() > 4 ) {
          attributeName += methodName.substring( 4 );
        }
        Object value = method.invoke( object );
        mapOutputBuilder.put( attributeName, value );
      }
    }
    return mapOutputBuilder;
  }

  @Override public void write( OutputStream outputStream ) throws IOException {
    outputBuilder.write( outputStream );
  }
}
