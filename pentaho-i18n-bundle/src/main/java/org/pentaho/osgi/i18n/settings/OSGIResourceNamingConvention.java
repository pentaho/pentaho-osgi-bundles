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
}
