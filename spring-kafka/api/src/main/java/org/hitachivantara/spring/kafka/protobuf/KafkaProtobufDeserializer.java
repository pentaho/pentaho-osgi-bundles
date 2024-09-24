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

package org.hitachivantara.spring.kafka.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class KafkaProtobufDeserializer<T extends MessageLite> implements Deserializer<T> {

  private final Parser<T> parser;

  /**
   * Returns a new instance of {@link KafkaProtobufDeserializer}.
   *
   * @param parser The Protobuf {@link Parser}.
   */
  public KafkaProtobufDeserializer( Parser<T> parser ) {
    this.parser = parser;
  }

  @Override
  public void configure( Map<String, ?> configs, boolean isKey ) {
    // no config is used
  }

  @Override
  public T deserialize( String topic, byte[] data ) {
    try {
      return parser.parseFrom( data );
    } catch ( InvalidProtocolBufferException e ) {
      throw new SerializationException( "Error deserializing from Protobuf message", e );
    }
  }

  @Override
  public void close() {
    // close not needed
  }
}
