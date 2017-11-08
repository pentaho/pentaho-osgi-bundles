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

public class OSGIResourceNamingConvention {

  public static final String RESOURCES_I18N_FOLDER = "i18n";
  public static final String RESOURCES_DEFAULT_EXTENSION = ".properties";

  public static final Pattern RESOURCE_NAME_PATTERN = Pattern.compile( "(.*/[^_]+)(.*).properties(\\.\\d+)?" );

  public static Matcher getResourceNameMatcher( String path ) {
    Matcher matcher = RESOURCE_NAME_PATTERN.matcher( path );

    boolean matches = matcher.matches();
    if ( matches ) {
      return matcher;
    }

    String message = "Path must be of the form prefix/filename[_internationalization].properties[.priority]";
    throw new IllegalArgumentException( message );
  }

  /**
   * Returns property priority propertyName must have message-name.properties.5 format (priority = 5)
   *
   * @param propertyName i18n resource name
   *
   * @return propertyPriority, default priority = 0
   */
  public static int getPropertyPriority( String propertyName ) {
    int priority = 0;
    Matcher matcher = getResourceNameMatcher( propertyName );

    String group = matcher.group( matcher.groupCount() );
    if ( group != null ) {
      priority = Integer.parseInt( group.substring( 1 ) );
    }

    return priority;
  }

  public static List<String> getCandidateNames( String name, Locale locale ) {
    List<String> result = new ArrayList<>();
    result.add( name );

    String language = locale.getLanguage();
    if ( !isStringEmpty( language ) ) {
      result.add( name + "_" + language  );

      String country = locale.getCountry();
      if ( !isStringEmpty( country ) ) {
        result.add( name + "_" + language + "_" + country );
      }
    }

    Collections.reverse( result );
    return result;
  }

  private static boolean isStringEmpty( String value ) {
    return value == null || value.isEmpty();
  }

}
