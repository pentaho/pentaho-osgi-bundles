/*!
 * Copyright 2010 - 2024 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.caching.spi;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.caching.api.Constants;

import javax.cache.configuration.CompleteConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
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
          put( Constants.CONFIG_TTL, String.valueOf( 60 * 2 ) ).
          put( Constants.CONFIG_TTL_RESET, Constants.ExpiryFunction.ACCESS.name() ).
          put( Constants.CONFIG_STORE_BY_VALUE, "false" ).
        build()
    );

    assertThat( configuration.getKeyType(), Matchers.<Class>equalTo( String.class ) );
    assertThat( configuration.getValueType(), Matchers.<Class>equalTo( List.class ) );

    assertFalse( configuration.isStoreByValue() );
    ExpiryPolicy expiryPolicy = configuration.getExpiryPolicyFactory().create();
    assertThat( expiryPolicy, instanceOf( AccessedExpiryPolicy.class ) );
    assertThat( expiryPolicy.getExpiryForAccess(), equalTo( new Duration( TimeUnit.MINUTES, 2 ) ) );
    assertThat( expiryPolicy.getExpiryForUpdate(), nullValue() );
  }
}
