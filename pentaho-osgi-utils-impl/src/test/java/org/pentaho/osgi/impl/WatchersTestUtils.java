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
 * Copyright 2016 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.osgi.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;

public class WatchersTestUtils {

  private static final String BUNDLE_NAME_PREFIX = "Bundle_";

  public static String getBundleName( long id ) {
    return BUNDLE_NAME_PREFIX + id;
  }

  public static String findKarafDebugOutput( List<String> messages, String header ) {
    for ( String message : messages ) {
      if ( message.startsWith( header ) ) {
        messages.remove( message );
        return message;
      }
    }

    // No debug output was found that starts with header
    return null;
  }

  public static void testEquivalentReports( String report1, String report2 ) {
    String[] splitedReport1 = report1.split( "\r\n?|\n" );
    String[] splitedReport2 = report2.split( "\r\n?|\n" );

    List<String> sortReport1 = new ArrayList<String>( Arrays.asList( splitedReport1 ) );
    Collections.sort( sortReport1 );

    List<String> sortReport2 = new ArrayList<String>( Arrays.asList( splitedReport2 ) );
    Collections.sort( sortReport2 );

    Assert.assertTrue( sortReport1.equals( sortReport2 ) );
  }
}
