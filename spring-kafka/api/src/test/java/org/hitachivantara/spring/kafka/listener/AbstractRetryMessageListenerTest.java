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
