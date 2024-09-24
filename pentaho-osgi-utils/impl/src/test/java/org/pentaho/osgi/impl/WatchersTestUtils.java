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
