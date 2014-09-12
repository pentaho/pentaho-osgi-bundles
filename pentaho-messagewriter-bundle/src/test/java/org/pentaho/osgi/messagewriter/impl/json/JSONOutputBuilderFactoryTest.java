/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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

package org.pentaho.osgi.messagewriter.impl.json;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by bryan on 9/12/14.
 */
public class JSONOutputBuilderFactoryTest {
  @Test
  public void testMediaType() {
    assertEquals( "application", new JSONOutputBuilderFactory().mediaType() );
  }

  @Test
  public void testSubType() {
    assertEquals( "json", new JSONOutputBuilderFactory().subType() );
  }

  @Test
  public void testCreateListOutputBuilder() {
    assertTrue( new JSONOutputBuilderFactory().createListOutputBuilder() != null );
  }

  @Test
  public void testCreateMapOutputBuilder() {
    assertTrue( new JSONOutputBuilderFactory().createMapOutputBuilder() != null );
  }

  @Test
  public void testCreatePrimitiveOutputBuilder() {
    assertTrue( new JSONOutputBuilderFactory().createPrimitiveOutputBuilder() != null );
  }
}
