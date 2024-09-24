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

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.listener.config.ContainerProperties;

import java.util.Collection;
import java.util.Map;

/**
 * Class that Kafka message listeners should extend. The implementations should call the
 * init method in order to make sure that the listener container is started.
 *
 * @param <K> The key type.
 * @param <V> The value type.
 */
public abstract class AbstractMessageListener<K, V> implements AcknowledgingMessageListener<K, V>, ConsumerSeekAware {

  private static final Logger logger = LoggerFactory.getLogger( AbstractMessageListener.class );

  protected ConsumerFactory<K, V> consumerFactory;
  protected String topic;
  protected String groupId;
  protected Runnable callbackOnPartitionsAssigned = () -> logger.debug( "Partition assigned for {}", topic );
  protected Runnable callbackOnPartitionsRevoked = () -> logger.debug( "Partition revoked for {}", topic );

  private MessageListenerContainer messageListenerContainer;

  /**
   * Starts the listener, creating the container if it hasn't been done yet.
   */
  public void start() {
    if ( this.messageListenerContainer == null ) {
      logger.info( "Listener container is still not assembled. Doing it now for topic {}", this.topic );
      init();
    }
    logger.info( "Starting listener: {}", this.messageListenerContainer );
    this.messageListenerContainer.start();
  }

  /**
   * Stops the listener
   * @param callback The callback that will be called when the container is stopped.
   */
  public void stop( Runnable callback ) {
    if ( this.messageListenerContainer != null ) {
      logger.info( "Stopping listener: {}", this.messageListenerContainer );
      this.messageListenerContainer.stop( callback );
    }
  }

  /**
   * Stops the listener
   */
  public void stop() {
    if ( this.messageListenerContainer != null ) {
      logger.info( "Stopping listener: {}", this.messageListenerContainer );
      this.messageListenerContainer.stop();
    }
  }

  /**
   * As default the current instance is the message listener. This may be override for example to
   * use a {@link org.springframework.kafka.listener.adapter.RetryingAcknowledgingMessageListenerAdapter}.
   * @return
   */
  protected AcknowledgingMessageListener<K, V> getMessageListener() {
    return this;
  }

  /**
   * Assembles the listener. The container listener is also started here.
   */
  public void init() {
    ContainerProperties containerProperties = buildContainerProperties();
    this.messageListenerContainer = buildMessageListenerContainer( consumerFactory, containerProperties );
  }

  /**
   * Builds the message listener to use in the init method.
   * @param consumerFactory the consumer factory.
   * @param containerProperties The container properties
   * @return A {@link MessageListenerContainer} instance.
   */
  protected MessageListenerContainer buildMessageListenerContainer( ConsumerFactory<K, V> consumerFactory, ContainerProperties containerProperties ) {
    return new KafkaMessageListenerContainer<>( consumerFactory, containerProperties );
  }

  /**
   * Builds the container properties. This implementation uses the local topic variable.
   * This container properties are used in the init method that is then passed to
   * the buildMessageListenerContainer method.
   *
   * Both the local topic, callbackOnPartitionsRevoked and callbackOnPartitionsAssigned variables
   * are used on this default implementation.
   *
   * The topic name is also used to set the listener group with the same value.
   *
   * The Ack mode is set to MANUAL_IMMEDIATE.
   * @return A {@link ContainerProperties} instance.
   */
  protected ContainerProperties buildContainerProperties() {
    ContainerProperties containerProperties = new ContainerProperties( getTopic() );
    containerProperties.setMessageListener( this.getMessageListener() );
    containerProperties.setGroupId( getGroupId() );
    containerProperties.setAckMode( AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE );
    containerProperties.setConsumerRebalanceListener( new ConsumerRebalanceListener() {
      @Override
      public void onPartitionsRevoked( Collection<TopicPartition> collection ) {
        if ( callbackOnPartitionsRevoked != null ) {
          callbackOnPartitionsRevoked.run();
        } else {
          logger.debug( "Partition revoked for {}", getTopic() );
        }
      }

      @Override
      public void onPartitionsAssigned( Collection<TopicPartition> collection ) {
        if ( callbackOnPartitionsAssigned != null ) {
          callbackOnPartitionsAssigned.run();
        } else {
          logger.debug( "Partition assigned for {}", getTopic() );
        }
      }
    } );
    return containerProperties;
  }


  @Override
  public void registerSeekCallback( ConsumerSeekCallback consumerSeekCallback ) {
    logger.info( "Register seek callback was invoked... nothing will be done" );
  }

  @Override
  public void onPartitionsAssigned( Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback ) {
    logger.info( "Partitions where assigned... nothing will be done" );
  }

  @Override
  public void onIdleContainer( Map<TopicPartition, Long> map, ConsumerSeekCallback consumerSeekCallback ) {
    logger.info( "Container idle was invoked... nothing will be done" );
  }

  /**
   * Sets the consumer factory.
   * @param consumerFactory The consumer factory to use.
   */
  public void setConsumerFactory( ConsumerFactory<K, V> consumerFactory ) {
    this.consumerFactory = consumerFactory;
  }

  /**
   * Get the consumer factory.
   * @return the consumer factory,
   */
  public ConsumerFactory<K, V> getConsumerFactory() {
    return consumerFactory;
  }

  /**
   * Sets the topic to use
   * @param topic The topic.
   */
  public void setTopic( String topic ) {
    this.topic = topic;
  }

  /**
   * Get the topic.
   * @return The topic.
   */
  public String getTopic() {
    return topic;
  }

  /**
   * Get the group id.
   * @return the group id.
   */
  public String getGroupId() {
    return groupId != null ? groupId : topic;
  }

  /**
   * Sets the group id to use. If this value is null it will use the topic value.
   * @param groupId the group id to use. Set to null to use the topic as the group id.
   */
  public void setGroupId( String groupId ) {
    this.groupId = groupId;
  }

  /**
   * Sets the partition assigned callback.
   * @param callbackOnPartitionsAssigned the callback to be called when the partition is assigned.
   */
  public void setCallbackOnPartitionsAssigned( Runnable callbackOnPartitionsAssigned ) {
    this.callbackOnPartitionsAssigned = callbackOnPartitionsAssigned;
  }

  /**
   * Get the partition assigned callback.
   * @return the partition assigned callback.
   */
  public Runnable getCallbackOnPartitionsAssigned() {
    return callbackOnPartitionsAssigned;
  }

  /**
   * Sets the partition revoked callback.
   * @param callbackOnPartitionsRevoked the callback to be called when the partition is revoked.
   */
  public void setCallbackOnPartitionsRevoked( Runnable callbackOnPartitionsRevoked ) {
    this.callbackOnPartitionsRevoked = callbackOnPartitionsRevoked;
  }

  /**
   * Get the current partition revoked callback.
   * @return the current partition revoked callback.
   */
  public Runnable getCallbackOnPartitionsRevoked() {
    return callbackOnPartitionsRevoked;
  }
}
