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

package org.pentaho.osgi.i18n.webservice;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class ResourceBundleRequestTest {
  @Test
  public void testSetLocale() throws Exception {
    ResourceBundleRequest resourceBundleRequest = new ResourceBundleRequest();
    String locale = "test-locale";
    resourceBundleRequest.setLocale( locale );
    assertEquals( locale, resourceBundleRequest.getLocale() );
  }

  @Test
  public void testSetWildcards() throws Exception {
    ResourceBundleRequest resourceBundleRequest = new ResourceBundleRequest();
    List<ResourceBundleWildcard> wildcards = mock( List.class );
    resourceBundleRequest.setWildcards( wildcards );
    assertEquals( wildcards, resourceBundleRequest.getWildcards() );
  }
}