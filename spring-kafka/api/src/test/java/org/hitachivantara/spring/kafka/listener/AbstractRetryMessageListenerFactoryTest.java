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
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AbstractRetryMessageListenerFactoryTest {

  private AbstractRetryMessageListenerFactory abstractRetryMessageListenerFactory;
  private AbstractRetryMessageListener abstractRetryMessageListener;

  @Before
  public void setup() {
    abstractRetryMessageListener = new Listener();
    abstractRetryMessageListenerFactory = new AbstractRetryMessageListenerFactory() {

      @Override
      protected AbstractMessageListener getMessageListenerInstance() {
        return abstractRetryMessageListener;
      }
    };
  }

  @Test
  public void getInstanceShouldReturnInstanceWithTopicAndGroupSet() {
    AbstractRetryMessageListener instance = abstractRetryMessageListenerFactory.getInstance( "topic", "groupId" );
    assertNotNull( instance );
    assertEquals( abstractRetryMessageListener, instance );
    assertEquals( "topic", instance.getTopic() );
    assertEquals( "groupId", instance.getGroupId() );
  }

  @Test
  public void getInstanceShouldReturnInstanceWithTopicAndGroupEqualToTopic() {
    AbstractRetryMessageListener instance = abstractRetryMessageListenerFactory.getInstance( "topic", null );
    assertNotNull( instance );
    assertEquals( abstractRetryMessageListener, instance );
    assertEquals( "topic", instance.getTopic() );
    assertEquals( "topic", instance.getGroupId() );
  }

  @Test
  public void getInstanceShouldHaveDefaultValuesForRetryAndBackOff() {
    AbstractRetryMessageListener instance = abstractRetryMessageListenerFactory.getInstance( "topic", "groupId" );
    assertNotNull( instance );

    assertEquals( SimpleRetryPolicy.DEFAULT_MAX_ATTEMPTS, instance.getRetryMaxAttempts() );
    assertEquals( ExponentialBackOffPolicy.DEFAULT_INITIAL_INTERVAL, instance.getBackOffInitialInterval() );
    assertEquals( ExponentialBackOffPolicy.DEFAULT_MAX_INTERVAL, instance.getBackOffIMaxInterval() );
    assertEquals( (Double) ExponentialBackOffPolicy.DEFAULT_MULTIPLIER, Double.valueOf( instance.getBackOffIMultiplier() ) );
  }

  @Test
  public void getInstanceShouldHaveCustomValuesForRetryAndBackOff() {
    abstractRetryMessageListenerFactory.setRetryMaxAttempts( 11 );
    abstractRetryMessageListenerFactory.setBackOffInitialInterval( 22L );
    abstractRetryMessageListenerFactory.setBackOffIMaxInterval( 33L );
    abstractRetryMessageListenerFactory.setBackOffIMultiplier( 44d );

    AbstractRetryMessageListener instance = abstractRetryMessageListenerFactory.getInstance( "topic", "groupId" );
    assertNotNull( instance );

    assertEquals( 11, instance.getRetryMaxAttempts() );
    assertEquals( 22L, instance.getBackOffInitialInterval() );
    assertEquals( 33L, instance.getBackOffIMaxInterval() );
    assertEquals( Double.valueOf( 44d ), Double.valueOf( instance.getBackOffIMultiplier() ) );
  }

  private class Listener extends AbstractRetryMessageListener {

    @Override public void onMessage( Object o, Acknowledgment acknowledgment ) {
      // noop
    }
  }
}
