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
