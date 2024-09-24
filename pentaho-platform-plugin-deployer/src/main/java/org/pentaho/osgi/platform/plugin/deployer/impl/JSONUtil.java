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

import org.json.simple.JSONValue;

import java.util.List;
import java.util.Map;

/**
 * Created by bryan on 9/3/14.
 */
public class JSONUtil {
  public String prettyPrintMapStringListString( Map<String, List<String>> map ) {
    StringBuilder sb = new StringBuilder();
    appendOpen( sb );
    for ( Map.Entry<String, List<String>> entry : map.entrySet() ) {
      writeKeyAndSeparator( sb, entry.getKey(), 2 );
      writeListOfStrings( sb, entry.getValue(), 4 );
      sb.append( ",\n" );
    }
    if ( map.size() > 0 ) {
      sb.setLength( sb.length() - 2 );
    }
    appendClose( sb );
    return sb.toString();
  }

  public String prettyPrintMapStringString( Map<String, String> map ) {
    StringBuilder sb = new StringBuilder();
    appendOpen( sb );
    for ( Map.Entry<String, String> entry : map.entrySet() ) {
      writeKeyAndSeparator( sb, entry.getKey(), 2 );
      sb.append( JSONValue.toJSONString( entry.getValue() ) );
      sb.append( ",\n" );
    }
    if ( map.size() > 0 ) {
      sb.setLength( sb.length() - 2 );
    }
    appendClose( sb );
    return sb.toString();
  }

  private void appendOpen( StringBuilder stringBuilder ) {
    stringBuilder.append( "{\n" );
  }

  private void appendClose( StringBuilder stringBuilder ) {
    stringBuilder.append( "\n}\n" );
  }

  private void writeKeyAndSeparator( StringBuilder stringBuilder, String key, int spaceBefore ) {
    for ( int i = 0; i < spaceBefore; i++ ) {
      stringBuilder.append( ' ' );
    }
    stringBuilder.append( JSONValue.toJSONString( key ) );
    stringBuilder.append( " : " );
  }

  private void writeListOfStrings( StringBuilder stringBuilder, List<String> list, int spaceBefore ) {
    stringBuilder.append( "[ \n" );
    for ( String entry : list ) {
      for ( int i = 0; i < spaceBefore; i++ ) {
        stringBuilder.append( ' ' );
      }
      stringBuilder.append( JSONValue.toJSONString( entry ) );
      stringBuilder.append( ",\n" );
    }
    stringBuilder.setLength( stringBuilder.length() - 2 );
    stringBuilder.append( " ]" );
  }
}
