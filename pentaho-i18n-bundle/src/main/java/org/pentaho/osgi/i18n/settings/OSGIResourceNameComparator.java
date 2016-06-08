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
