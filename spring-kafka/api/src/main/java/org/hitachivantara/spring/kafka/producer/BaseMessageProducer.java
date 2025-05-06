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


package org.hitachivantara.spring.kafka.producer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

public class BaseMessageProducer {

  protected static final Log log = LogFactory.getLog( BaseMessageProducer.class );
  private String kafkaTopic;
  private KafkaTemplate<String, byte[]> kafkaTemplate;
  private String kafkaBootstrapServers;
  private static final String ENVIRONMENT_JOB_UUID = "uuid";

  public BaseMessageProducer( KafkaTemplate<String, byte[]> template ) {
    this.kafkaTemplate = template;
  }

  private KafkaTemplate<String, byte[]> getKafkaTemplate() {
    return kafkaTemplate;
  }

  public void setKafkaTemplate( KafkaTemplate<String, byte[]> kafkaTemplate ) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public String getKafkaTopic() {
    return kafkaTopic;
  }

  public void setKafkaTopic( String kafkaLogTopic ) {
    this.kafkaTopic = kafkaLogTopic;
  }

  public String getKafkaBootstrapServers() {
    return kafkaBootstrapServers;
  }

  public void setKafkaBootstrapServers( String kafkaBootstrapServers ) {
    this.kafkaBootstrapServers = kafkaBootstrapServers;
  }

  public String getJobUUID() {
    // Read the value from environment variables
    return System.getenv( ENVIRONMENT_JOB_UUID );
  }

  /**
   * The bootstrapServer parameter will be in the form host:port. Check if host or port is empty
   * @param kafkaBootstrapServers
   * @return boolean
   */
  private boolean isValidKafkaBootstrapServer( String kafkaBootstrapServers ) {
    boolean isValid = true;
    String[] bootStrapServer = kafkaBootstrapServers.split( ":" );
    if ( bootStrapServer.length > 0 && ( bootStrapServer[0].isEmpty() || bootStrapServer[1].isEmpty() ) ) {
      log.info( "Kafka host or port is not provided. Cannot post status message" );
      isValid = false;
    }
    return isValid;
  }

  public void sendMessage( String key, byte[] message ) {
    // Check if the bootstrap url is provided
    if ( !isValidKafkaBootstrapServer( getKafkaBootstrapServers() ) ) {
      return;
    }
    // Validate kafka topic
    if ( getKafkaTopic().isEmpty() ) {
      log.info( "Kafka topic is not provided. Cannot post status message" );
      return;
    }

    // Send the message to kafka
    ListenableFuture<SendResult<String, byte[]>> resultFuture = getKafkaTemplate().send( getKafkaTopic(), key, message );
    resultFuture.addCallback( new ListenableFutureCallback<SendResult<String, byte[]>>() {
      @Override
      public void onFailure( Throwable throwable ) {
        log.info( String.format( "Failed to post message for key %s to topic %s", key, getKafkaTopic() ) );
      }

      @Override
      public void onSuccess( SendResult<String, byte[]> stringSendResult ) {
        log.debug( String.format( "Posted message for key %s to topic %s", key, getKafkaTopic() ) );
      }
    } );
  }
}
