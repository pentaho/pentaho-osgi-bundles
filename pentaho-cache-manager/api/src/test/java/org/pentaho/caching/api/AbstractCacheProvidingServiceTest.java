/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.caching.api;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.cache.configuration.CompleteConfiguration;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author nhudak
 */
@RunWith( MockitoJUnitRunner.class )
public class AbstractCacheProvidingServiceTest {
  @Mock( answer = Answers.CALLS_REAL_METHODS ) AbstractCacheProvidingService service;

  @Test
  public void testCreateConfiguration() throws Exception {
    CompleteConfiguration<String, List> configuration = service.createConfiguration(
      String.class, List.class, ImmutableMap.<String, String>builder().
        build()
    );

    assertThat( configuration.getKeyType(), Matchers.<Class>equalTo( String.class ) );
    assertThat( configuration.getValueType(), Matchers.<Class>equalTo( List.class ) );
  }
}
