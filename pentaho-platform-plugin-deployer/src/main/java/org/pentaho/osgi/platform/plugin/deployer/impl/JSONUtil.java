/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
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
