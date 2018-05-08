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
 * Copyright 2014 - 2018 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.osgi.platform.webjars.utils;

import org.junit.Test;
import org.osgi.framework.Version;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.*;

public class VersionTest {

  @Test
  public void testGetVersion() throws Exception {
    {
      Version v = RequireJsGenerator.VersionParser.parseVersion( "1.2.3" );
      assertEquals("1.2.3", v.toString());
    }

    {
      Version v = RequireJsGenerator.VersionParser.parseVersion("1.2");
      assertEquals("1.2.0", v.toString());
    }

    {
      Version v = RequireJsGenerator.VersionParser.parseVersion("1");
      assertEquals("1.0.0", v.toString());
    }

    {
      Version v = RequireJsGenerator.VersionParser.parseVersion("1.0-SNAPSHOT");
      assertEquals("1.0.0.SNAPSHOT", v.toString());
    }

    {
      Version v = RequireJsGenerator.VersionParser.parseVersion("1.0.2-SNAPSHOT");
      assertEquals("1.0.2.SNAPSHOT", v.toString());
    }

    {
      Version v = RequireJsGenerator.VersionParser.parseVersion("707ebd9e05");
      assertEquals("707.0.0.ebd9e05", v.toString());
    }

    {
      Version v = RequireJsGenerator.VersionParser.parseVersion("ebd9e05707");
      assertEquals("0.0.0.ebd9e05707", v.toString());
    }

    {
      Version v = RequireJsGenerator.VersionParser.parseVersion("");
      assertEquals("0.0.0", v.toString());
    }

    {
      Version v = RequireJsGenerator.VersionParser.parseVersion(null);
      assertEquals("0.0.0", v.toString());
    }

    {
      Version v = RequireJsGenerator.VersionParser.parseVersion("***");
      assertEquals("0.0.0", v.toString());
    }

  }

  @Test
  public void testConstructorException() throws IllegalAccessException, InstantiationException {
    final Class<RequireJsGenerator.VersionParser> versionStaticClass = RequireJsGenerator.VersionParser.class;
    final Constructor<?> c = versionStaticClass.getDeclaredConstructors()[0];
    c.setAccessible(true);

    Throwable targetException = null;
    try {
      c.newInstance();
    } catch (InvocationTargetException e) {
      targetException = e.getTargetException();
    }

    assertNotNull(targetException);
    assertEquals(targetException.getClass(), InstantiationException.class);
  }
}
