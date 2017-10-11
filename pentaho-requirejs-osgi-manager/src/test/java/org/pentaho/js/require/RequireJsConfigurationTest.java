/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
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
 *
 ******************************************************************************/

package org.pentaho.js.require;

import org.junit.Test;
import org.osgi.framework.Bundle;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Created by bryan on 9/2/14.
 */
public class RequireJsConfigurationTest {
  @Test
  public void testConstructor() {
    Bundle bundle = mock( Bundle.class );
    List<String> configs = new ArrayList<String>();
    configs.add( "test" );
    RequireJsConfiguration configuration = new RequireJsConfiguration( bundle, configs );
    assertEquals( bundle, configuration.getBundle() );
    assertEquals( configs, configuration.getRequireConfigurations() );
  }
}
