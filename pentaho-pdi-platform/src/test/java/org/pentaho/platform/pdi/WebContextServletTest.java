/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pentaho.platform.pdi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IPlatformWebResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by rfellows on 9/9/16.
 */
@RunWith( MockitoJUnitRunner.class )
public class WebContextServletTest {
  WebContextServlet webContextServlet;

  private IPlatformWebResource jsFile;
  private IPlatformWebResource txtFile;

  @Before
  public void setUp() throws Exception {
    webContextServlet = new WebContextServlet();
    jsFile = new PlatformWebResource( "analyzer", "scripts/includeMe.js" );
    txtFile = new PlatformWebResource( "analyzer", "scripts/includeMe.txt" );
  }

  @Test
  public void testGetWebResources_NoMatches() throws Exception {
    List<String> webResources = webContextServlet.getWebResources( "analyzer", ".*\\.js" );
    assertNotNull( webResources );
    assertEquals( 0, webResources.size() );
  }

  @Test
  public void testGetWebResources_Match() throws Exception {
    webContextServlet.addPlatformWebResource( jsFile );
    webContextServlet.addPlatformWebResource( txtFile );
    List<String> webResources = webContextServlet.getWebResources( "analyzer", ".*\\.js" );
    assertNotNull( webResources );
    assertEquals( 1, webResources.size() );
    assertEquals( "scripts/includeMe.js", webResources.get( 0 ) );
  }

  @Test
  public void testAppendWebResourcesToDoc() throws Exception {
    StringBuilder sb = new StringBuilder();
    List<String> resources = new ArrayList<>();
    resources.add( "scripts/includeMe.js" );
    resources.add( "scripts/includeMeToo.js" );

    webContextServlet.appendJsWebResources( sb, resources );

    String result = sb.toString();
    String expected = "document.write(\"<script type='text/javascript' src='/scripts/includeMe.js'></scr\"+\"ipt>\");\n"
      + "document.write(\"<script type='text/javascript' src='/scripts/includeMeToo.js'></scr\"+\"ipt>\");\n";
    assertEquals( expected, result );

  }
}