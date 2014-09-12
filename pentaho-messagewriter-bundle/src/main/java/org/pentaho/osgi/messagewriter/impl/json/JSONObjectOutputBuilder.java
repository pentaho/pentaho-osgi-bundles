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
import org.pentaho.osgi.messagewriter.OutputException;
import org.pentaho.osgi.messagewriter.builder.ObjectOutputBuilder;
import org.pentaho.osgi.messagewriter.builder.Outputter;

/**
 * Created by bryan on 9/11/14.
 */
public class JSONObjectOutputBuilder extends JSONOutputBuilder implements ObjectOutputBuilder {
  private JSONOutputBuilder jsonOutputBuilder;

  @Override public JSONStreamAware getJSONStreamAware() {
    return jsonOutputBuilder.getJSONStreamAware();
  }

  @Override public void set( Object value ) throws OutputException {
    new Outputter( new JSONOutputBuilderFactory() {
      @Override public JSONListOutputBuilder createListOutputBuilder() {
        JSONListOutputBuilder listOutputBuilder = super.createListOutputBuilder();
        jsonOutputBuilder = listOutputBuilder;
        return listOutputBuilder;
      }

      @Override public JSONMapOutputBuilder createMapOutputBuilder() {
        JSONMapOutputBuilder mapOutputBuilder = super.createMapOutputBuilder();
        jsonOutputBuilder = mapOutputBuilder;
        return mapOutputBuilder;
      }

      @Override public JSONPrimitiveOutputBuilder createPrimitiveOutputBuilder() {
        JSONPrimitiveOutputBuilder primitiveOutputBuilder = super.createPrimitiveOutputBuilder();
        jsonOutputBuilder = primitiveOutputBuilder;
        return primitiveOutputBuilder;
      }
    } ).set( value );
  }
}
