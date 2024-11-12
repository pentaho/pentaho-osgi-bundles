/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.hitachivantara.spring.kafka.listener;

import org.junit.Before;
import org.junit.Test;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.listener.adapter.RetryingAcknowledgingMessageListenerAdapter;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class AbstractRetryMessageListenerTest {

  private AbstractRetryMessageListener abstractRetryMessageListener;

  @Before
  public void setup() {
    abstractRetryMessageListener = spy( new AbstractRetryMessageListener() {
      @Override
      public void onMessage( Object o, Acknowledgment acknowledgment ) {
        //no op
      }
    } );
  }

  @Test
  public void testMessageListenerFromGetShouldContainTheRetryTemplate() {
    RetryTemplate retryTemplate = mock( RetryTemplate.class );
    when( abstractRetryMessageListener.getRetryTemplate() ).thenReturn( retryTemplate );

    assertNotNull( abstractRetryMessageListener.getMessageListener() );
    assertTrue( abstractRetryMessageListener.getMessageListener() instanceof RetryingAcknowledgingMessageListenerAdapter );
    RetryingAcknowledgingMessageListenerAdapter acknowledgingMessageListener = (RetryingAcknowledgingMessageListenerAdapter) abstractRetryMessageListener.getMessageListener();
    assertEquals( retryTemplate, acknowledgingMessageListener.getRetryTemplate() );
  }

  @Test
  public void testGetRetryTemplateShouldHaveTheDefaultSimpleRetryPolicy() {
    RetryPolicy retryPolicy = abstractRetryMessageListener.getRetryPolicy();
    assertNotNull( retryPolicy );
    assertTrue( retryPolicy instanceof SimpleRetryPolicy );
    assertEquals( SimpleRetryPolicy.DEFAULT_MAX_ATTEMPTS, ( (SimpleRetryPolicy) retryPolicy ).getMaxAttempts() );
  }

  @Test
  public void testGetRetryTemplateShouldHaveTheCustomRetryMaxAttempts() {
    abstractRetryMessageListener.setRetryMaxAttempts( 99 );
    RetryPolicy retryPolicy = abstractRetryMessageListener.getRetryPolicy();
    assertNotNull( retryPolicy );
    assertTrue( retryPolicy instanceof SimpleRetryPolicy );
    assertEquals( 99, ( (SimpleRetryPolicy) retryPolicy ).getMaxAttempts() );
  }

  @Test
  public void testGetBackOffPolicyShouldHaveDefaultExponentialBackOffPolicy() {
    BackOffPolicy backOffPolicy = abstractRetryMessageListener.getBackOffPolicy();
    assertNotNull( backOffPolicy );
    assertTrue( backOffPolicy instanceof ExponentialBackOffPolicy );
    assertEquals( ExponentialBackOffPolicy.DEFAULT_INITIAL_INTERVAL, ( (ExponentialBackOffPolicy) backOffPolicy ).getInitialInterval() );
    assertEquals( ExponentialBackOffPolicy.DEFAULT_MAX_INTERVAL, ( (ExponentialBackOffPolicy) backOffPolicy ).getMaxInterval() );
    assertEquals( Double.valueOf( ExponentialBackOffPolicy.DEFAULT_MULTIPLIER ), Double.valueOf( ( (ExponentialBackOffPolicy) backOffPolicy ).getMultiplier() ) );
  }

  @Test
  public void testGetBackOffPolicyShouldHaveCustomBackOffPropertiesSet() {
    abstractRetryMessageListener.setBackOffInitialInterval( 55L );
    abstractRetryMessageListener.setBackOffIMaxInterval( 66L );
    abstractRetryMessageListener.setBackOffIMultiplier( 1.5d );
    BackOffPolicy backOffPolicy = abstractRetryMessageListener.getBackOffPolicy();
    assertNotNull( backOffPolicy );
    assertTrue( backOffPolicy instanceof ExponentialBackOffPolicy );
    assertEquals( 55L, ( (ExponentialBackOffPolicy) backOffPolicy ).getInitialInterval() );
    assertEquals( 66L, ( (ExponentialBackOffPolicy) backOffPolicy ).getMaxInterval() );
    assertEquals( Double.valueOf( 1.5d ), Double.valueOf( ( (ExponentialBackOffPolicy) backOffPolicy ).getMultiplier() ) );
  }

  @Test
  public void testBuildMessageListenerAdaptorShouldHaveTheRetryTemplateSet() {
    RetryTemplate mockRetryTemplate = mock( RetryTemplate.class );
    AcknowledgingMessageListener mockListener = mock( AcknowledgingMessageListener.class );
    AcknowledgingMessageListener messageListenerAdapter = abstractRetryMessageListener.buildMessageListenerAdapter( mockListener, mockRetryTemplate );
    assertTrue( messageListenerAdapter instanceof RetryingAcknowledgingMessageListenerAdapter );
    assertEquals( mockRetryTemplate, ( (RetryingAcknowledgingMessageListenerAdapter) messageListenerAdapter ).getRetryTemplate() );
  }

  @Test
  public void testRetryExceptionMessageIsSetAndIsRuntimeException() {
    AbstractRetryMessageListener.RetryMessageError exception = new AbstractRetryMessageListener.RetryMessageError("message");
    assertTrue( exception instanceof RuntimeException );
    assertEquals( "message", exception.getMessage() );
  }
}
