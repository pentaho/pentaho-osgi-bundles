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

package org.pentaho.osgi.i18n.settings;

import java.util.Comparator;
import java.util.regex.Matcher;

/**
 * Created by Viktoryia_Klimenka on 6/7/2016.
 */
public class OSGIResourceNameComparator implements Comparator<String> {
  @Override public int compare( String o1, String o2 ) {
    Matcher o1Matcher = OSGIResourceNamingConvention.getResourceNameMatcher( o1 );
    Matcher o2Matcher = OSGIResourceNamingConvention.getResourceNameMatcher( o2 );
    String name1 = o1Matcher.group( 1 ) + o1Matcher.group( 2 );
    String name2 = o2Matcher.group( 1 ) + o2Matcher.group( 2 );
    if ( name1.compareTo( name2 ) == 0 ) {
      int priority1 = OSGIResourceNamingConvention.getPropertyPriority( o1 );
      int priority2 = OSGIResourceNamingConvention.getPropertyPriority( o2 );
      return priority1 - priority2;
    } else {
      return name1.compareTo( name2 );
    }
  }
}
