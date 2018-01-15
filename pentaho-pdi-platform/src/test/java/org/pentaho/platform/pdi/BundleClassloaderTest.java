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
package org.pentaho.platform.pdi;

import org.junit.Test;
import org.osgi.framework.Bundle;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

/**
 * Created by nbaker on 8/16/16.
 */
public class BundleClassloaderTest {
  @Test
  public void getName() throws Exception {
    BundleClassloader bundleClassloader = new BundleClassloader( mock( Bundle.class ), "test" );
    assertEquals( "test", bundleClassloader.getName() );
    URL url = bundleClassloader.findResource( "OSGI-INF/blueprint/blueprint.xml" );
    assertNull( url );
  }

}