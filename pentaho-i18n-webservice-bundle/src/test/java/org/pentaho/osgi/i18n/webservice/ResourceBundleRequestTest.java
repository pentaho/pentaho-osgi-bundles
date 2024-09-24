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