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
