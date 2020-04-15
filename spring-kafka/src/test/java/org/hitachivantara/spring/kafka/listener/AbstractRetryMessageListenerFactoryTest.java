/*
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2020 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */

package org.hitachivantara.spring.kafka.listener;

import org.junit.Before;
import org.junit.Test;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AbstractRetryMessageListenerFactoryTest {

  private AbstractRetryMessageListenerFactory abstractRetryMessageListener;
  private AbstractRetryMessageListener abstractMessageListener;

  @Before
  public void setup() {
    abstractMessageListener = mock( AbstractRetryMessageListener.class );
    abstractRetryMessageListener = new AbstractRetryMessageListenerFactory() {

      @Override
      protected AbstractMessageListener getMessageListenerInstance() {
        return abstractMessageListener;
      }
    };
  }

  @Test
  public void getInstance() {
    AbstractRetryMessageListener instance = abstractRetryMessageListener.getInstance( "topic" );

    assertNotNull( instance );
    assertEquals( abstractMessageListener, instance );
    verify( abstractMessageListener, times( 2 ) ).setTopic( "topic" );
    verify( abstractMessageListener ).setRetryMaxAttempts( SimpleRetryPolicy.DEFAULT_MAX_ATTEMPTS );
    verify( abstractMessageListener ).setBackOffInitialInterval( ExponentialBackOffPolicy.DEFAULT_INITIAL_INTERVAL );
    verify( abstractMessageListener ).setBackOffIMaxInterval( ExponentialBackOffPolicy.DEFAULT_MAX_INTERVAL );
    verify( abstractMessageListener ).setBackOffIMultiplier( ExponentialBackOffPolicy.DEFAULT_MULTIPLIER );
  }

  @Test
  public void getInstanceCustomProps() {
    abstractRetryMessageListener.setRetryMaxAttempts( 11 );
    abstractRetryMessageListener.setBackOffInitialInterval( 22L );
    abstractRetryMessageListener.setBackOffIMaxInterval( 33L );
    abstractRetryMessageListener.setBackOffIMultiplier( 44d );

    AbstractRetryMessageListener instance = abstractRetryMessageListener.getInstance( "topic" );

    assertNotNull( instance );
    assertEquals( abstractMessageListener, instance );
    verify( abstractMessageListener, times( 2 ) ).setTopic( "topic" );
    verify( abstractMessageListener ).setRetryMaxAttempts( 11 );
    verify( abstractMessageListener ).setBackOffInitialInterval(  22L );
    verify( abstractMessageListener ).setBackOffIMaxInterval( 33L );
    verify( abstractMessageListener ).setBackOffIMultiplier( 44d );
  }
}
