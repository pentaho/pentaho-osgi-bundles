/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

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
