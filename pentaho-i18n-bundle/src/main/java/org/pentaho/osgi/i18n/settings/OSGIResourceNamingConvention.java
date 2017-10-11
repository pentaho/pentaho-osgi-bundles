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
 * Copyright 2016 - 2017 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.osgi.i18n.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Viktoryia_Klimenka on 5/30/2016.
 */
public class OSGIResourceNamingConvention {
  public static final String RESOURCES_ROOT_FOLDER = "i18n";
  public static final String RESOURCES_DEFAULT_EXTENSION = ".properties";

  public static final Pattern RESOURCE_NAME_PATTERN =
    Pattern.compile( "(.*/[^_]+)(.*).properties(\\.\\d+)?" );

  public static Matcher getResourceNameMatcher( String path ) {
    Matcher matcher = RESOURCE_NAME_PATTERN.matcher( path );
    boolean matches = matcher.matches();
    if ( matches ) {
      return matcher;
    } else {
      throw new IllegalArgumentException(
        "Path must be of the form prefix/filename[_internationalization].properties[.priority]" );
    }
  }

  /**
   * Returns property priority propertyName must have message-name.properties.5 format (priority = 5)
   *
   * @param propertyName i18n resource name
   * @return propertyPriority, default priority = 0
   */
  public static int getPropertyPriority( String propertyName ) {
    int priority = 0;
    Matcher matcher = getResourceNameMatcher( propertyName );
    String groop = matcher.group( matcher.groupCount() );
    if ( groop != null ) {
      priority = Integer.parseInt( groop.substring( 1 ) );
    }
    return priority;
  }

  public static List<String> getCandidateNames( String name, Locale locale ) {
    List<String> result = new ArrayList<String>();
    String current = name;
    result.add( current );
    String language = locale.getLanguage();
    if ( language.length() > 0 ) {
      current += "_" + language;
      result.add( current );
      String country = locale.getCountry();
      if ( country.length() > 0 ) {
        current += "_" + country;
        result.add( current );
      }
    }
    Collections.reverse( result );
    return result;
  }
}
