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
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.support.Acknowledgment;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class AbstractMessageListenerTest {

  private AbstractMessageListener innerMockedAbstractMessageListener;
  private AbstractMessageListener abstractMessageListener;
  private ContainerProperties containerProperties;
  private MessageListenerContainer abstractMessageListenerContainer;
  private ConsumerFactory consumerFactory;

  private ArgumentCaptor<ConsumerFactory> consumerFactoryCaptor = ArgumentCaptor.forClass( ConsumerFactory.class );
  private ArgumentCaptor<ContainerProperties> containerPropertiesCaptor = ArgumentCaptor.forClass( ContainerProperties.class );

  @Before
  public void setup() {
    containerProperties = mock( ContainerProperties.class );
    abstractMessageListenerContainer = mock( MessageListenerContainer.class );
    consumerFactory = mock( ConsumerFactory.class );

    innerMockedAbstractMessageListener = spy( new AbstractMessageListener() {
      @Override
      public void onMessage( Object o, Acknowledgment acknowledgment ) {

      }

      @Override
      protected ContainerProperties buildContainerProperties() {
        return containerProperties;
      }

      @Override
      protected MessageListenerContainer buildMessageListenerContainer( ConsumerFactory consumerFactory, ContainerProperties containerProperties ) {
        return abstractMessageListenerContainer;
      }
    } );
    innerMockedAbstractMessageListener.setConsumerFactory( consumerFactory );

    abstractMessageListener = spy( new AbstractMessageListener() {
      @Override
      public void onMessage( Object o, Acknowledgment acknowledgment ) {

      }
    } );
    abstractMessageListener.setConsumerFactory( consumerFactory );
  }

  @Test
  public void testStartWithoutPreviousAssemble() {
    innerMockedAbstractMessageListener.start();

    verify( innerMockedAbstractMessageListener ).assembleListenerContainer();
    verify( abstractMessageListenerContainer ).start();
  }

  @Test
  public void testStartWithPreviousAssemble() {
    innerMockedAbstractMessageListener.assembleListenerContainer();
    innerMockedAbstractMessageListener.start();

    verify( innerMockedAbstractMessageListener, times( 1 ) ).assembleListenerContainer();
    verify( abstractMessageListenerContainer ).start();
  }

  @Test
  public void testStopCallbackWithoutPreviousAssemble() {
    AtomicBoolean callBackCalled = new AtomicBoolean( false );
    Runnable callback = () -> callBackCalled.set( true );

    innerMockedAbstractMessageListener.stop( callback );

    verifyZeroInteractions( abstractMessageListenerContainer );
  }

  @Test
  public void testStopCallbackWithPreviousAssemble() {
    AtomicBoolean callBackCalled = new AtomicBoolean( false );
    Runnable callback = () -> callBackCalled.set( true );

    innerMockedAbstractMessageListener.assembleListenerContainer();
    innerMockedAbstractMessageListener.stop( callback );

    verify( abstractMessageListenerContainer ).stop( callback );
  }

  @Test
  public void testStopWithoutPreviousAssemble() {
    innerMockedAbstractMessageListener.stop();

    verifyZeroInteractions( abstractMessageListenerContainer );
  }

  @Test
  public void testStopWithPreviousAssemble() {
    innerMockedAbstractMessageListener.assembleListenerContainer();
    innerMockedAbstractMessageListener.stop();

    verify( abstractMessageListenerContainer ).stop();
  }

  @Test
  public void testGetMessageListener() {
    assertEquals( innerMockedAbstractMessageListener, innerMockedAbstractMessageListener.getMessageListener() );
    assertEquals( abstractMessageListener, abstractMessageListener.getMessageListener() );
  }

  @Test
  public void testAssembleListenerContainer() {
    innerMockedAbstractMessageListener.assembleListenerContainer();

    verify( innerMockedAbstractMessageListener ).buildMessageListenerContainer( consumerFactoryCaptor.capture(), containerPropertiesCaptor.capture() );
    verify( innerMockedAbstractMessageListener ).buildContainerProperties();
    assertEquals( consumerFactory, consumerFactoryCaptor.getValue() );
    assertEquals( containerProperties, containerPropertiesCaptor.getValue() );
  }

  @Test
  public void testBuildMessageListenerContainer() {
    when( containerProperties.getTopics() ).thenReturn( new String[]{ "Topic"} );
    MessageListenerContainer returnObj = abstractMessageListener.buildMessageListenerContainer( consumerFactory, containerProperties );
    assertNotNull( returnObj );
    assertTrue( returnObj instanceof MessageListenerContainer );
  }

  @Test
  public void testBuildContainerProperties() {
    abstractMessageListener.setTopic( "topic" );
    ContainerProperties containerPropertiesObj = abstractMessageListener.buildContainerProperties();

    assertEquals( "topic", containerPropertiesObj.getTopics()[0] );
    assertEquals( "topic", containerPropertiesObj.getGroupId() );
    assertEquals( AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE, containerPropertiesObj.getAckMode() );
    assertEquals( abstractMessageListener, containerPropertiesObj.getMessageListener() );
  }

  @Test
  public void testBuildContainerPropertiesCallbacks() {
    AtomicBoolean assigned = new AtomicBoolean( false );
    AtomicBoolean revoked = new AtomicBoolean( false );
    Runnable assignedCallback = () -> assigned.set( true );
    Runnable revokedCallback = () -> revoked.set( true );
    abstractMessageListener.setCallbackOnPartitionsAssigned( assignedCallback );
    abstractMessageListener.setCallbackOnPartitionsRevoked( revokedCallback );
    abstractMessageListener.setTopic( "topic" );

    ContainerProperties containerPropertiesObj = abstractMessageListener.buildContainerProperties();

    assertFalse( assigned.get() );
    assertFalse( revoked.get() );

    containerPropertiesObj.getConsumerRebalanceListener().onPartitionsAssigned( null );
    assertTrue( assigned.get() );
    assertFalse( revoked.get() );

    containerPropertiesObj.getConsumerRebalanceListener().onPartitionsRevoked( null );
    assertTrue( assigned.get() );
    assertTrue( revoked.get() );
  }
}
