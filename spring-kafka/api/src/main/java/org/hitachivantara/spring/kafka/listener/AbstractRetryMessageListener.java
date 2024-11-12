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
