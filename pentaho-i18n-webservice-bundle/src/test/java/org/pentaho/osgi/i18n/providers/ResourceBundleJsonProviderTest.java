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
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ResourceBundleJsonProviderTest {
  private ResourceBundleJsonProvider jsonProvider;

  @Before
  public void setup() {
    this.jsonProvider = new ResourceBundleJsonProvider();
  }

  @Test
  public void testIsWritable() {
    MediaType jsonType = MediaType.APPLICATION_JSON_TYPE;
    MediaType wildcardType = MediaType.WILDCARD_TYPE;

    assertTrue( this.jsonProvider.isWriteable( ResourceBundle.class, null, null, jsonType ) );
    assertFalse( this.jsonProvider.isWriteable( Object.class, null, null, jsonType ) );
    assertFalse( this.jsonProvider.isWriteable( ResourceBundle.class, null, null, wildcardType ) );
  }

  @Test
  public void testGetSize() {
    long expectedSize = -1;

    long size = this.jsonProvider.getSize( null, null, null, null, null );
    assertEquals( expectedSize, size );
  }

  @Test
  public void testJsonWrite() throws IOException {
    Map<String, String> props = new HashMap<>();
    props.put( "key1", "value1" );
    props.put( "key2", "value2" );

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ResourceBundle resourceBundle = makeResourceBundle( props );
    MediaType jsonType = MediaType.APPLICATION_JSON_TYPE;

    this.jsonProvider.writeTo( resourceBundle, ResourceBundle.class, null, null, jsonType,
        null, byteArrayOutputStream );

    JSONObject result = getWriteToResult( byteArrayOutputStream );

    int expectedSize = props.size();

    assertEquals( expectedSize, result.size() );
    assertEquals( props.get( "key1" ), result.get( "key1" ) );
    assertEquals( props.get( "key2" ), result.get( "key2" ) );
  }

  private ResourceBundle makeResourceBundle( final Map<String, String> map ) {
    return new ListResourceBundle() {
      @Override
      protected Object[][] getContents() {
        Object[][] result = new Object[ map.size() ][];
        int index = 0;
        for ( Map.Entry<String, String> entry : map.entrySet() ) {
          Object[] entryObj = new Object[ 2 ];
          entryObj[ 0 ] = entry.getKey();
          entryObj[ 1 ] = entry.getValue();
          result[ index++ ] = entryObj;
        }
        return result;
      }
    };
  }

  private JSONObject getWriteToResult( ByteArrayOutputStream out ) {
    return (JSONObject) JSONValue.parse( out.toString() );
  }
}
