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
