/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
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
