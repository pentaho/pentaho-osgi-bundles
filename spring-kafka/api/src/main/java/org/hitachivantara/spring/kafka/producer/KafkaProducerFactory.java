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


package org.hitachivantara.spring.kafka.producer;

import org.apache.kafka.common.serialization.Serializer;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;

public class KafkaProducerFactory  {
  private static final String DEFAULT_KAFKA_PRODUCER_FACTORY_BEAN_NAME = "defaultKafkaProducerFactory";
  private BlueprintContainer container;

  public KafkaProducerFactory( BlueprintContainer container ) {
    this.container = container;
  }

  public ProducerFactory createProducerFactory( Serializer keySerializer, Serializer valueSerializer ) {
    DefaultKafkaProducerFactory producerFactory = (DefaultKafkaProducerFactory) this.container.getComponentInstance( DEFAULT_KAFKA_PRODUCER_FACTORY_BEAN_NAME );
    producerFactory.setKeySerializer( keySerializer );
    producerFactory.setValueSerializer( valueSerializer );
    return producerFactory;
  }

}
