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

import com.google.protobuf.MessageLite;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KafkaProtobufSerializerTest {

  @Test
  public void testSerialize() {
    MessageLite messageLite = mock( MessageLite.class );
    byte[] mockByteArray = "test".getBytes();
    when( messageLite.toByteArray() ).thenReturn( mockByteArray );
    KafkaProtobufSerializer<MessageLite> serializer = new KafkaProtobufSerializer();
    byte[] result = serializer.serialize( "topic", messageLite );
    assertEquals( mockByteArray, result );
  }
}
