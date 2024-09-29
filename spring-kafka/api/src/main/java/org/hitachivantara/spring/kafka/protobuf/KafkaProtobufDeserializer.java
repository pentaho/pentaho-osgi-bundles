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
