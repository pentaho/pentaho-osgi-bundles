# Spring Kafka

## Goal
The goal of this module is to be able to have a common code base to send and/or receive messages from a Kafka broker
using Spring Kafka as the underlying library.

This module also enforces that Google Protobuf is used as the message encoding.

## Type Converter

Through out the below examples we will use `type-converters` elements which are needed because the type erasure when injecting beans in 
Karaf is not following the same algorithm as Java.
Because of that methods which accept Generic types don't recognize the existing methods as valid ones to use in the injection.

Further info can be seen in the `osgi-blueprint-api` module class [GenericsTypeConverter.java](https://github.com/pentaho/pentaho-osgi-bundles/blob/54c4e1d0ad1324962714d5c405318331b4f223f4/osgi-blueprint-api/src/main/java/org/hitachivantara/osgi/service/blueprint/GenericsTypeConverter.java)
javadoc.

See also below issues in apache Aries project to more details:
[Apache ARIES-1500](https://issues.apache.org/jira/browse/ARIES-1500)
[Apache ARIES-1607](https://issues.apache.org/jira/browse/ARIES-1607)

## Producer

To produce messages to Kafka topics the following blueprint definitions should be used:

```xml
<type-converters>
  <bean class="org.hitachivantara.osgi.service.blueprint.GenericsTypeConverter">
    <argument>
      <map key-type="java.lang.Class" value-type="java.lang.Class">
        <entry key="org.apache.kafka.common.serialization.StringSerializer" value="org.apache.kafka.common.serialization.Serializer" />
        <entry key="org.hitachivantara.spring.kafka.protobuf.KafkaProtobufSerializer" value="org.apache.kafka.common.serialization.Serializer" />
        <entry key="org.springframework.kafka.core.DefaultKafkaProducerFactory" value="org.springframework.kafka.core.ProducerFactory" />

        <entry key="org.springframework.kafka.core.KafkaTemplate" value="org.springframework.kafka.core.KafkaTemplate" />
      </map>
    </argument>
  </bean>
</type-converters>

<!-- Producer properties -->
<bean id="kafkaProducerProperties" class="java.util.HashMap">
  <argument>
    <map key-type="java.lang.String" value-type="java.lang.Object">
      <entry key="bootstrap.servers" value="localhost:9092"/>
      <entry key="retries" value="0"/>
      <entry key="batch.size" value="16384"/>
      <entry key="linger.ms" value="1"/>
      <entry key="buffer.memory" value="33554432"/>
    </map>
  </argument>
</bean>

<!-- Producer Kafka Template -->
<bean id="kafkaProxyProducerKafkaTemplate" class="org.springframework.kafka.core.KafkaTemplate">
  <argument> <!-- type converter used on this argument -->
    <bean class="org.springframework.kafka.core.DefaultKafkaProducerFactory">
      <argument ref="kafkaProducerProperties"/>
      <argument> <!-- type converter used on this argument -->
        <bean class="org.apache.kafka.common.serialization.StringSerializer" />
      </argument>
      <argument> <!-- type converter used on this argument -->
        <bean class="org.hitachivantara.spring.kafka.protobuf.KafkaProtobufSerializer" />
      </argument>
    </bean>
  </argument>
  <argument type="java.lang.Boolean" value="true" /> <!-- auto flush -->
</bean> 
```

A `BaseMessageProducer` class was added to be able to produce messages just by defining a bean for that class. That way the usage of 
the Kafka Template in our code is black boxed. To be able to use it a bean like this must be defined:
```xml
<bean id="mySimpleKafkaProducerBean" class="org.hitachivantara.spring.kafka.producer.BaseMessageProducer">
  <property name="kafkaTemplate" ref="kafkaProxyProducerKafkaTemplate"/>
  <property name="kafkaTopic" value="myKafkaTopic"/>
</bean>
```

### Type converter
Notice that the `org.springframework.kafka.core.KafkaTemplate` entry in the type converter is needed in order to 
inject the example bean `kafkaProxyProducerKafkaTemplate` in our applications beans like this:
```xml
  <bean id="ourApplicationBean" class="com.hitachivantara.ApplicationBean">
    <property name="kafkaTemplate" ref="kafkaProxyProducerKafkaTemplate"/> <!-- type converter used on this argument -->
  </bean>
``` 
```java
public class ApplicationBean {
  private KafkaTemplate<String, Message> kafkaTemplate;
  
  public void setKafkaTemplate( KafkaTemplate<String, Message> kafkaTemplate ) {
    this.kafkaTemplate = kafkaTemplate;
  }
}
```

### KafkaTemplate
The easiest way to send messages is using the Spring Kafka Template API defined in `org.springframework.kafka.core.KafkaTemplate`.
That class has two constructors. Both accept as the first argument an interface `org.apache.kafka.clients.producer.Producer` implementation instance
and the second optional argument is a `boolean` to configure the auto flush ability of the Kafka Template instance.

#### Producer Factory
Spring Kafka provides a default implementation for that interface `org.springframework.kafka.core.DefaultKafkaProducerFactory`. We
default to use that, for which we must provide an `HashMap` with the Kafka connection and library properties.

Those properties are defined in this example in the `kafkaProducerProperties`, and notice that only the basic ones are being used.
Kafka and Spring Kafka documentation should be used to check which properties exist and how they may influence the application.

#### Producer Key & Value Serializer
When we send messages through Kafka we can use the Kafka provided serializers for `org.apache.kafka.common.serialization.IntegerSerializer`, 
`org.apache.kafka.common.serialization.StringSerializer`, etc. But since we advocate the usage of protobuf we must use binary transport
definition but with specific serializers for Protobuf.

For this end we created a `org.apache.kafka.common.serialization.Serializer<T>` interface implementation in 
`org.hitachivantara.spring.kafka.protobuf.KafkaProtobufSerializer`. The usage of this Serializer implementation allows us to be able
to send Protobuf messages directly through the Kafka Template like this:
```java
MyProtobufMessage messageBody = MyProtobufMessage
  .newBuilder()
  .setValue("Hello World!")
  .build();

kafkaTemplate.send(
  "KafkaTopic",
  "KEY_1",
  messageBody
);
```
Notice how the `send` method second parameter accepts a String because we used Kafka `StringSerializer` for the key, and the
third parameter accepts a `com.google.protobuf.MessageLite` because we used our `KafkaProtobufSerializer` for the value.

## Consumer

The consumer part of the messages follows some of the patterns used in the producer but adds a layer of complexity because
we may have different strategies to read messages from the Kafka brokers. To consume messages from Kafka topics the following 
blueprint definitions should be used:

```xml
<type-converters>
  <bean class="org.hitachivantara.osgi.service.blueprint.GenericsTypeConverter">
    <argument>
      <map key-type="java.lang.Class" value-type="java.lang.Class">
        <entry key="org.springframework.kafka.core.DefaultKafkaConsumerFactory" value="org.springframework.kafka.core.ConsumerFactory" />
        <entry key="org.apache.kafka.common.serialization.StringDeserializer" value="org.apache.kafka.common.serialization.Deserializer" />
        <entry key="org.hitachivantara.spring.kafka.protobuf.KafkaProtobufDeserializer" value="org.apache.kafka.common.serialization.Deserializer" />
        <entry key="com.hitachivantara.protobuf.api.proto.SimpleMessage$1" value="com.google.protobuf.Parser" />
        <entry key="com.hitachivantara.protobuf.api.proto.ComplexMessage$1" value="com.google.protobuf.Parser" />
      </map>
    </argument>
  </bean>
</type-converters>

<!-- Consumer properties -->
<bean id="kafkaConsumerProperties" class="java.util.HashMap">
  <argument>
    <map key-type="java.lang.String" value-type="java.lang.Object">
      <entry key="bootstrap.servers" value="localhost:9092"/>
      <entry key="group.id" value="Default_Consumer_Group_ID"/>
      <entry key="enable.auto.commit" value="false"/>
      <entry key="session.timeout.ms" value="15000"/>
    </map>
  </argument>
</bean>

<!-- Simple Message Listener-->
<bean id="kafkaSimpleMessageListener" class="com.hitachivantara.kafka.SimpleListener"
      init-method="start" destroy-method="stop" >
  <property name="consumerFactory"> <!-- type converter used on this argument -->
    <bean class="org.springframework.kafka.core.DefaultKafkaConsumerFactory">
      <argument ref="kafkaConsumerProperties"/>
      <argument> <!-- type converter used on this argument -->
        <bean class="org.apache.kafka.common.serialization.StringDeserializer" />
      </argument>
      <argument> <!-- type converter used on this argument -->
        <bean class="org.hitachivantara.spring.kafka.protobuf.KafkaProtobufDeserializer">
          <argument> <!-- type converter used on this argument -->
            <bean class="com.hitachivantara.protobuf.api.proto.SimpleMessage" factory-method="parser"/>
          </argument>
        </bean>
      </argument>
    </bean>
  </property>
  <property name="topic" value="SimpleTopic"/>
  <property name="groupId" value="SimpleConsumerGroupId"/>
</bean>

<!-- Complex Listener Factory -->
<bean id="kafkaComplexMessageListenerFactory"  class="com.hitachivantara.kafka.ComplexListenerFactory">
  <property name="consumerFactory"> <!-- type converter used on this argument -->
    <bean class="org.springframework.kafka.core.DefaultKafkaConsumerFactory">
      <argument ref="kafkaConsumerProperties"/>
      <argument> <!-- type converter used on this argument -->
        <bean class="org.apache.kafka.common.serialization.StringDeserializer" />
      </argument>
      <argument> <!-- type converter used on this argument -->
        <bean class="org.hitachivantara.spring.kafka.protobuf.KafkaProtobufDeserializer">
          <argument> <!-- type converter used on this argument -->
            <bean class="com.hitachivantara.protobuf.api.proto.ComplexMessage" factory-method="parser"/>
          </argument>
        </bean>
      </argument>
    </bean>
  </property>
  <property name="retryMaxAttempts" value="10"/>
  <property name="someProperty" value="somethingRelevantForTheListener"/>
</bean>
```

### Simple Consumer
The simple consumer is just a basic class where we extend the [AbstractMessageListener.java](https://github.com/pentaho/pentaho-osgi-bundles/blob/54c4e1d0ad1324962714d5c405318331b4f223f4/spring-kafka/api/src/main/java/org/hitachivantara/spring/kafka/listener/AbstractMessageListener.java).
That class implementation can be as simple as this:
```java
public class SimpleListener extends AbstractMessageListener<String, SimpleMessage> {
    
  // If we need custom logic on the topic name we can override this
  //@Override public String getTopic() {
  //  return "CustomTopic";
  //}

  // If we need custom logic on the group ID we can override this
  //@Override public String getGroupId() {
  //  return "CustomGroupID";
  //}

  @Override
  public void onMessage( ConsumerRecord<String, SimpleMessage> record, Acknowledgment acknowledgment ) {
    // DO SOMETHING WITH THE MESSAGE
    try {
      
    } finally {
      // ACK the message
      acknowledgment.acknowledge();
  }
}
```

This class when declared in the blueprint as above will create a message consumer for Kafka messages sent to the `SimpleTopic` 
in the group `SimpleConsumerGroupId` (both topic and group can be further customized as shown above in Java code).

The message, and consequently the consumer offset, will only be committed after the message is acknowledge (it's possible to
have a consumer that auto acknowledge messages by using `org.springframework.kafka.listener.MessageListener` implementation).

It's important to notice that the bean definition has `init-method="start" destroy-method="stop"` defined, which mean that the listener will be
started when our blueprint bean is initiated and stoped when it is destroyed.

### Complex Consumer
There is many times the need to be able to dynamically create message consumers. To provide that we have listener factories. In this example
we will demonstrate that factory usage. This example will also show how to use the retry mechanism to retry messages.

The bean that we defined is a factory for a `org.hitachivantara.spring.kafka.listener.AbstractRetryMessageListener` instances.
That factory follows a vanilla way to implement a factory patern where some of the final bean (`AbstractRetryMessageListener`) properties
are initialized, and some run time properties (the topic and group ID) are received in runtime when new instances are created.

The factory is a generic type object which means that it will work with any implementation of the `AbstractRetryMessageListener` class.

We define our factory implementation like this:
```java
public class ComplexListenerFactory extends AbstractRetryMessageListenerFactory<String, ComplexMessage, ComplexListener> {

  // this is set in the blueprint definition
  private String someProperty;
  
  public void setSomeProperty( String someProperty ) {
    this.someProperty = someProperty;
  }

  @Override
  protected ComplexListener getMessageListenerInstance() {
    ComplexListener complexListener = new ComplexListener();
    complexListener.setSomeProperty( someProperty );
    return complexListener;
  }
}
```

The corresponding `ComplexListener` implementation could be something like this:
```java
public class ComplexListener extends AbstractRetryMessageListener<String, ComplexMessage> {
  
  // this is set by the factory above, but can also be set in blueprint if we don't use a factory for this listener
  private String someProperty;
    
  public void setSomeProperty( String someProperty ) {
    this.someProperty = someProperty;
  }
  
 // If we need custom logic on the topic name we can override this
  //@Override public String getTopic() {
  //  return "CustomTopic";
  //}

  // If we need custom logic on the group ID we can override this
  //@Override public String getGroupId() {
  //  return "CustomGroupID";
  //}
  
  @Override
  public void onMessage( ConsumerRecord<String, ComplexMessage> record, Acknowledgment acknowledgment ) {
    try {
      // DO SOMETHING WITH THE MESSAGE
      
      // throw RuntimeException to force the message retry in case of a transient error
      if ( isTransientError() ) {
        throw new RuntimeException( "ERROR PROCESSING MESSAGE... RETRY WILL HAPPEN ACCORDING TO CONFIG" );
      }
      
      // ACK the message
      acknowledgment.acknowledge();

    } finally {
      // release resources
    }
  }
}
```

Notice that on the above implementation we are not acknowledging the message in the finally block because the message can be retried  if
a `RuntimeException` is thrown, and in that case we don't want to ACK the message. 

The retry listener can use four properties to manage how the message is consumed when retrying.
* `retryMaxAttempts` - the number of attempts of re-processing an inbound message (default: 3)
* `backOffInitialInterval` - the backoff initial interval on retry (default: 1000)
* `backOffIMaxInterval` - the maximum backoff interval (default: 10000)
* `backOffIMultiplier` - the backoff multiplier (default: 2.0)
Using the default values we ensure that the message will be retied 3 times. After the first failure there will be a 1000ms delay before
retrying again, and after that we will multiply by 2.0 that delay for each retry (1000, then 2000, then 4000). The delay between retries
will never exceed the 10000ms.

### Consumer value message type
When consuming messages we can also specify the type of the keys and values for the messages in the topic, much like when producing messages.

However there is a substantially difference when using the provided [KafkaProtobufDeserializer.java](https://github.com/pentaho/pentaho-osgi-bundles/blob/54c4e1d0ad1324962714d5c405318331b4f223f4/spring-kafka/api/src/main/java/org/hitachivantara/spring/kafka/protobuf/KafkaProtobufDeserializer.java)
class. While the [KafkaProtobufSerializer.java](https://github.com/pentaho/pentaho-osgi-bundles/blob/54c4e1d0ad1324962714d5c405318331b4f223f4/spring-kafka/api/src/main/java/org/hitachivantara/spring/kafka/protobuf/KafkaProtobufSerializer.java)
was able to serialize any type of Protobuf message, the deserializer must know which type of message we are using in order to be able
to use the protobuf message type `parseFrom(byte[])` method.

For this reason when defining the deserializer we must pass one argument, and declare the protobuf message inner class as a type in 
the type converter.
```xml
<entry key="com.hitachivantara.protobuf.api.proto.SimpleMessage$1" value="com.google.protobuf.Parser" />
<entry key="com.hitachivantara.protobuf.api.proto.ComplexMessage$1" value="com.google.protobuf.Parser" />

(...)

<argument> <!-- type converter used on this argument -->
  <bean class="org.hitachivantara.spring.kafka.protobuf.KafkaProtobufDeserializer">
    <argument> <!-- type converter used on this argument -->
      <bean class="com.hitachivantara.protobuf.api.proto.ComplexMessage" factory-method="parser"/>
    </argument>
  </bean>
</argument>
```