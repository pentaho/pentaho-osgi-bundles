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

import org.springframework.kafka.core.ConsumerFactory;

public abstract class AbstractMessageListenerFactory<K, V, L extends AbstractMessageListener> {

  private ConsumerFactory<K, V> consumerFactory;

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
   * Classes that extend this class should provide a way to obtain the message listener. This message
   * listener is used when the getInstance method is called.
   * @return should return an instance of the <L> type.
   */
  protected abstract L getMessageListenerInstance();

  /**
   * Gets an instance of the generic <L> with the local consumer factory, topic and
   * groupId setup.
   * @param topic the topic to associate to the listener.
   * @return A <L> instance of the type parametrized.
   */
  public L getInstance( String topic, String groupId ) {
    L messageListener = getMessageListenerInstance();
    messageListener.setConsumerFactory( this.consumerFactory );
    messageListener.setTopic( topic );
    messageListener.setGroupId( groupId );
    return messageListener;
  }
}
