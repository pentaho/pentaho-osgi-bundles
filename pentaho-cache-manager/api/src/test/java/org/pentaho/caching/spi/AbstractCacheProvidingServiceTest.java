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
