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

package org.pentaho.osgi.messagewriter.impl.json;

import org.json.simple.JSONStreamAware;
import org.junit.Test;
import org.pentaho.osgi.messagewriter.OutputException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by bryan on 9/12/14.
 */
public class JSONObjectOutputBuilderTest {
  @Test
  public void testAdd() throws OutputException, IOException {
    JSONObjectOutputBuilder jsonObjectOutputBuilder = new JSONObjectOutputBuilder();
    jsonObjectOutputBuilder.set( 2 );
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    jsonObjectOutputBuilder.write( byteArrayOutputStream );
    assertEquals( "2", byteArrayOutputStream.toString( "UTF-8" ) );
  }

  @Test
  public void testAddList() throws OutputException, IOException {
    JSONObjectOutputBuilder jsonObjectOutputBuilder = new JSONObjectOutputBuilder();
    jsonObjectOutputBuilder.set( new ArrayList<Integer>( Arrays.asList( 2 ) ) );
    JSONStreamAware result = jsonObjectOutputBuilder.getJSONStreamAware();
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    jsonObjectOutputBuilder.write( byteArrayOutputStream );
    assertEquals( "[2]", byteArrayOutputStream.toString( "UTF-8" ) );
  }

  @Test
  public void testAddMap() throws OutputException, IOException {
    JSONObjectOutputBuilder jsonObjectOutputBuilder = new JSONObjectOutputBuilder();
    Map<String, Object> map = new HashMap<String, Object>();
    map.put( "test", 2 );
    jsonObjectOutputBuilder.set( map );
    JSONStreamAware result = jsonObjectOutputBuilder.getJSONStreamAware();
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    jsonObjectOutputBuilder.write( byteArrayOutputStream );
    assertEquals( "{\"test\":2}", byteArrayOutputStream.toString( "UTF-8" ) );
  }
}
