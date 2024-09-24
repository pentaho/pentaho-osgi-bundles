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
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;

public class KafkaProtobufDeserializerTest {

  @Test
  public void testDeserialize() throws InvalidProtocolBufferException {
    Parser<MessageLite> parser = mock( Parser.class );
    KafkaProtobufDeserializer<MessageLite> deserializer = new KafkaProtobufDeserializer( parser );
    deserializer.deserialize( "topic", "data".getBytes() );
    verify( parser ).parseFrom( "data".getBytes() );
    deserializer.configure( null, false );
    deserializer.close();
    verifyNoMoreInteractions( parser );
  }

  @Test
  public void testConfigurerDoesntInteractParser() {
    Parser<MessageLite> parser = mock( Parser.class );
    KafkaProtobufDeserializer<MessageLite> deserializer = new KafkaProtobufDeserializer( parser );
    deserializer.configure( null, false );
    verifyNoInteractions( parser );
  }

  @Test
  public void testCloseDoesntInteractParser() {
    Parser<MessageLite> parser = mock( Parser.class );
    KafkaProtobufDeserializer<MessageLite> deserializer = new KafkaProtobufDeserializer( parser );
    deserializer.close();
    verifyNoInteractions( parser );
  }

  @Test( expected = SerializationException.class )
  public void testInvalidProtocolBufferException() throws InvalidProtocolBufferException {
    Parser<MessageLite> parser = mock( Parser.class );
    doThrow( new InvalidProtocolBufferException( "" ) ).when( parser ).parseFrom( (byte[]) any() );
    KafkaProtobufDeserializer<MessageLite> deserializer = new KafkaProtobufDeserializer( parser );
    deserializer.deserialize( "topic", "data".getBytes() );
  }
}
