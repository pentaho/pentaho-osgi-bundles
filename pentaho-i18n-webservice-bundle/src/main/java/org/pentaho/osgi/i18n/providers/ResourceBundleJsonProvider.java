/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.osgi.i18n.providers;

import org.json.simple.JSONObject;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.ResourceBundle;

@Provider
@Produces( MediaType.APPLICATION_JSON )
public class ResourceBundleJsonProvider  implements MessageBodyWriter<ResourceBundle> {

  @Override
  public boolean isWriteable( Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType ) {
    boolean isJsonMediaType = MediaType.APPLICATION_JSON_TYPE.equals( mediaType );

    return isJsonMediaType && ResourceBundle.class.isAssignableFrom( type );
  }

  @Override
  public long getSize( ResourceBundle resourceBundle, Class<?> type, Type genericType,
                       Annotation[] annotations, MediaType mediaType ) {
    return -1;
  }

  @Override
  public void writeTo( ResourceBundle resourceBundle, Class<?> type, Type genericType, Annotation[] annotations,
                       MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream )
      throws IOException, WebApplicationException {

    JSONObject resourceBundleJsonObject = new JSONObject();
    for ( String key : Collections.list( resourceBundle.getKeys() ) ) {
      resourceBundleJsonObject.put( key, resourceBundle.getString( key ) );
    }

    OutputStreamWriter outputStreamWriter = null;
    try {
      outputStreamWriter = new OutputStreamWriter( entityStream, StandardCharsets.UTF_8 );
      resourceBundleJsonObject.writeJSONString( outputStreamWriter );
    } finally {
      if ( outputStreamWriter != null ) {
        outputStreamWriter.flush();
      }
    }
  }
}
