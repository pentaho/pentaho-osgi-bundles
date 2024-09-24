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
