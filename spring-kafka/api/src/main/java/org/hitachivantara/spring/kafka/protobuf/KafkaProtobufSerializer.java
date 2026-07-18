/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.hitachivantara.spring.kafka.protobuf;

import com.google.protobuf.MessageLite;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class KafkaProtobufSerializer<T extends MessageLite> implements Serializer<T> {

  @Override
  public void configure( Map<String, ?> configs, boolean isKey ) {
    // nothing to configure
  }

  @Override
  public byte[] serialize( String topic, T data ) {
    return data.toByteArray();
  }

  @Override
  public void close() {
    // close not needed
  }
}
