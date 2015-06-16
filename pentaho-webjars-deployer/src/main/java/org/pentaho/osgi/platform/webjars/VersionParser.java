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
 * Copyright 2014 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.osgi.platform.webjars;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nbaker on 11/25/14.
 */
public class VersionParser {
  private static Logger logger = LoggerFactory.getLogger( VersionParser.class );

  private static Version DEFAULT = new Version(0,0,0);
  private static Pattern VERSION_PAT = Pattern.compile( "([0-9]+)?(?:\\.([0-9]*)(?:\\.([0-9]*))?)?[\\.-]?(.*)" );
  private static Pattern CLASSIFIER_PAT = Pattern.compile( "[a-zA-Z0-9_\\-]+" );

  public static Version parseVersion( String incomingVersion ) {
    if( StringUtils.isEmpty( incomingVersion ) ){
      return DEFAULT;
    }
    Matcher m = VERSION_PAT.matcher( incomingVersion );
    if ( m.matches() == false ) {
      return DEFAULT;
    } else {
      String s_major = m.group( 1 );
      String s_minor = m.group( 2 );
      String s_patch = m.group( 3 );
      String classifier = m.group( 4 );
      Integer major = 0;
      Integer minor = 0;
      Integer patch = 0;

      if( !StringUtils.isEmpty( s_major ) ) {
        try {
          major = Integer.parseInt( s_major );
        } catch ( NumberFormatException e ) {
          logger.warn( "Major version part not an integer: " + s_major );
        }
      }

      if( !StringUtils.isEmpty( s_minor ) ) {
        try {
          minor = Integer.parseInt( s_minor );
        } catch ( NumberFormatException e ) {
          logger.warn( "Minor version part not an integer: " + s_minor );
        }
      }

      if( !StringUtils.isEmpty( s_patch ) ) {
        try {
          patch = Integer.parseInt( s_patch );
        } catch ( NumberFormatException e ) {
          logger.warn( "Patch version part not an integer: " + s_patch );
        }
      }

      if ( classifier != null ) {
        // classifiers cannot have a '.'
        classifier = classifier.replaceAll( "\\.", "_" );

        // Classifier characters must be in the following ranges a-zA-Z0-9_\-
        if ( !CLASSIFIER_PAT.matcher( classifier ).matches() ) {
          logger.warn( "Provided Classifier not valid for OSGI, ignoring" );
          classifier = null;
        }
      }
      if ( classifier != null ) {
        return new Version( major, minor, patch, classifier );
      } else {
        return new Version( major, minor, patch );
      }

    }
  }
}
