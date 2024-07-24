/*
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2020-2024 Hitachi Vantara. All rights reserved.
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
