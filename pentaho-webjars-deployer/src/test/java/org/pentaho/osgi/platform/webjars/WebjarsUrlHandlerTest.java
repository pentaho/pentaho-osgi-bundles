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

import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import static org.junit.Assert.assertEquals;

public class WebjarsUrlHandlerTest {

  @Test
  public void testOpenConnection() throws Exception {
    WebjarsUrlHandler handler = new WebjarsUrlHandler();
    URL.setURLStreamHandlerFactory( new URLStreamHandlerFactory() {
      @Override public URLStreamHandler createURLStreamHandler( String protocol ) {
        if ( "pentaho-webjars".equals( protocol ) ) {
          return new WebjarsUrlHandler();
        }
        return null;
      }
    } );
    File f = new File( "src/test/resources/testInput.jar" );

    URLConnection urlConnection = handler.openConnection( new URL( "pentaho-webjars:" + f.toURI().toURL() ) );
    assertEquals( f.toURI().toURL(), urlConnection.getURL().toString() );
  }

}