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
