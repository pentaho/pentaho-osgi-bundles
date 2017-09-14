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
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.caching.spi;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
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
