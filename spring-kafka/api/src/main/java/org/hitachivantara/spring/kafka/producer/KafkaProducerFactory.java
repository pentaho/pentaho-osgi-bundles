/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
