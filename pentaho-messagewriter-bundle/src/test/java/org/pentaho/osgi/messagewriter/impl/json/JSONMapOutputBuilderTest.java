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

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
import org.junit.Test;
import org.pentaho.osgi.messagewriter.OutputException;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by bryan on 9/12/14.
 */
public class JSONMapOutputBuilderTest {
  @Test
  public void testAdd() throws OutputException, IOException {
    JSONMapOutputBuilder jsonMapOutputBuilder = new JSONMapOutputBuilder();
    jsonMapOutputBuilder.put( "two", 2 );
    JSONStreamAware result = jsonMapOutputBuilder.getJSONStreamAware();
    assertTrue( result instanceof JSONObject );
    JSONObject jsonObject = (JSONObject) result;
    assertEquals( 1, jsonObject.size() );
    StringWriter sw = new StringWriter();
    ( (JSONOutputBuilder) jsonObject.get( "two" ) ).getJSONStreamAware().writeJSONString( sw );
    assertEquals( "2", sw.toString() );
  }
}
