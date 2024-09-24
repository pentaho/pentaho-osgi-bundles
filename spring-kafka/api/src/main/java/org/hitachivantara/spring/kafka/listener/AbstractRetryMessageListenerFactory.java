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

import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;

public abstract class AbstractRetryMessageListenerFactory<K, V, L extends AbstractRetryMessageListener> extends AbstractMessageListenerFactory<K, V, L> {

  private int retryMaxAttempts = SimpleRetryPolicy.DEFAULT_MAX_ATTEMPTS;
  private long backOffInitialInterval = ExponentialBackOffPolicy.DEFAULT_INITIAL_INTERVAL;
  private long backOffIMaxInterval = ExponentialBackOffPolicy.DEFAULT_MAX_INTERVAL;
  private double backOffIMultiplier = ExponentialBackOffPolicy.DEFAULT_MULTIPLIER;

  /**
   * The number of maximum attempts that the message will be processed.
   * The message will only be retried if an exception is thrown, not if it is not acknowledged.
   * If no ack is done, and no exception is thrown the offset is increased but other consumers can read that message.
   * @param retryMaxAttempts
   */
  public void setRetryMaxAttempts( int retryMaxAttempts ) {
    this.retryMaxAttempts = retryMaxAttempts;
  }

  /**
   * An initial interval in ms to backoff when retrying to process the message.
   * @param backOffInitialInterval
   */
  public void setBackOffInitialInterval( long backOffInitialInterval ) {
    this.backOffInitialInterval = backOffInitialInterval;
  }

  /**
   * The maximum interval to backoff after applying the multiplier to the initial interval n times.
   * @param backOffIMaxInterval
   */
  public void setBackOffIMaxInterval( long backOffIMaxInterval ) {
    this.backOffIMaxInterval = backOffIMaxInterval;
  }

  /**
   * The multiplier to apply to the initial backoff (the value accrues) interval on every retry.
   * @param backOffIMultiplier
   */
  public void setBackOffIMultiplier( double backOffIMultiplier ) {
    this.backOffIMultiplier = backOffIMultiplier;
  }

  /**
   * Gets the retryMaxAttempts value.
   * @return the retryMaxAttempts value.
   */
  public int getRetryMaxAttempts() {
    return retryMaxAttempts;
  }

  /**
   * Gets the backOffInitialInterval value.
   * @return the backOffInitialInterval value.
   */
  public long getBackOffInitialInterval() {
    return backOffInitialInterval;
  }

  /**
   * Gets the backOffIMaxInterval value.
   * @return the backOffIMaxInterval value.
   */
  public long getBackOffIMaxInterval() {
    return backOffIMaxInterval;
  }

  /**
   * Gets the backOffIMultiplier value.
   * @return the backOffIMultiplier value.
   */
  public double getBackOffIMultiplier() {
    return backOffIMultiplier;
  }

  /**
   * Gets a <T> generic class instance. The retryMaxAttempts, backOffInitialInterval,
   * backOffIMaxInterval and backOffIMultiplier are relayed to that instance.
   *
   * @param topic the topic to associate to the listener.
   * @param groupId the group to associate the listener.
   * @return A instance of the <T> generic.
   */
  @Override
  public L getInstance( String topic, String groupId ) {
    L messageListener = super.getInstance( topic, groupId );
    messageListener.setRetryMaxAttempts( retryMaxAttempts );
    messageListener.setBackOffInitialInterval( backOffInitialInterval );
    messageListener.setBackOffIMaxInterval( backOffIMaxInterval );
    messageListener.setBackOffIMultiplier( backOffIMultiplier );
    return messageListener;
  }
}
