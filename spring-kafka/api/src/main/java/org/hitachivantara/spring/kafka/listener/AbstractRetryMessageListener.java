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

import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.listener.adapter.RetryingAcknowledgingMessageListenerAdapter;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

public abstract class AbstractRetryMessageListener<K, V> extends AbstractMessageListener<K, V> {

  protected int retryMaxAttempts = SimpleRetryPolicy.DEFAULT_MAX_ATTEMPTS;
  protected long backOffInitialInterval = ExponentialBackOffPolicy.DEFAULT_INITIAL_INTERVAL;
  protected long backOffIMaxInterval = ExponentialBackOffPolicy.DEFAULT_MAX_INTERVAL;
  protected double backOffIMultiplier = ExponentialBackOffPolicy.DEFAULT_MULTIPLIER;

  /**
   * The value to use on the {@link SimpleRetryPolicy} policy used on the
   * {@link RetryTemplate} applied to the message listener.
   * @param retryMaxAttempts
   */
  public void setRetryMaxAttempts( int retryMaxAttempts ) {
    this.retryMaxAttempts = retryMaxAttempts;
  }

  /**
   * The initial back off interval to use on the {@link ExponentialBackOffPolicy} policy
   * used on the {@link RetryTemplate} applied to the message listener.
   * @param backOffInitialInterval
   */
  public void setBackOffInitialInterval( long backOffInitialInterval ) {
    this.backOffInitialInterval = backOffInitialInterval;
  }

  /**
   * The initial backoff maximum interval to use on the {@link ExponentialBackOffPolicy} policy
   * used on the {@link RetryTemplate} applied to the message listener.
   * @param backOffIMaxInterval
   */
  public void setBackOffIMaxInterval( long backOffIMaxInterval ) {
    this.backOffIMaxInterval = backOffIMaxInterval;
  }

  /**
   * The initial backoff interval multiplier to use on the {@link ExponentialBackOffPolicy} policy
   * used on the {@link RetryTemplate} applied to the message listener.
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
   * Gets a acknowledgeable message listener with a retry template applied configured with a max attempts
   * retry policy and a exponential backoff policy.
   * @return the {@link AcknowledgingMessageListener} instance.
   */
  @Override
  protected AcknowledgingMessageListener<K, V> getMessageListener() {
    return this.buildMessageListenerAdapter( super.getMessageListener(), getRetryTemplate() );
  }

  /**
   * Get the retry template to use.
   * @return A {@link RetryTemplate} instance.
   */
  protected RetryTemplate getRetryTemplate() {
    RetryTemplate retryTemplate = new RetryTemplate();
    retryTemplate.setRetryPolicy( getRetryPolicy() );
    retryTemplate.setBackOffPolicy( getBackOffPolicy() );
    return retryTemplate;
  }

  /**
   * Gets the back off policy between retries to apply to the retry template.
   * The default implementation for this returns a {@link ExponentialBackOffPolicy}
   * configured with the local backOffInitialInterval, backOffIMaxInterval and
   * backOffIMultiplier values.
   * @return a {@link BackOffPolicy} instance.
   */
  protected BackOffPolicy getBackOffPolicy() {
    ExponentialBackOffPolicy exponentialBackOffPolicy = new ExponentialBackOffPolicy();
    exponentialBackOffPolicy.setInitialInterval( backOffInitialInterval );
    exponentialBackOffPolicy.setMaxInterval( backOffIMaxInterval );
    exponentialBackOffPolicy.setMultiplier( backOffIMultiplier );
    return exponentialBackOffPolicy;
  }

  /**
   * Obtains the retry police that should be applied in the retry template.
   * The default implementation for this returns a {@link SimpleRetryPolicy} configured
   * with the local retryMaxAttempts value.
   * @return a {@link RetryPolicy} instance.
   */
  protected RetryPolicy getRetryPolicy() {
    SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
    simpleRetryPolicy.setMaxAttempts( retryMaxAttempts );
    return simpleRetryPolicy;
  }

  protected AcknowledgingMessageListener<K, V> buildMessageListenerAdapter( AcknowledgingMessageListener<K, V> messageListener, RetryTemplate retryTemplate ) {
    return new RetryingAcknowledgingMessageListenerAdapter( messageListener, retryTemplate );
  }

  /**
   * An exception class that can be used to force a message retry based on a retry template and it's associated
   * policies.
   */
  public static class RetryMessageError extends RuntimeException {
    public RetryMessageError( String message ) {
      super( message );
    }
  }
}
