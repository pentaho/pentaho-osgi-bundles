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
