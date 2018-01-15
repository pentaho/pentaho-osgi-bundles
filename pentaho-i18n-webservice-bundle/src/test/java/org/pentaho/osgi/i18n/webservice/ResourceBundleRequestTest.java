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