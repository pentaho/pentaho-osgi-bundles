# Spring Kafka

## Introduction
The goal of this module is to be able to have a common code base to send and/or receive messages from a Kafka broker
using [Spring Kafka](https://spring.io/projects/spring-kafka) as the underlying library. 

##### Protobuf
We consider that using Google Protobuf should be the preferred way to send messages through Kafka, and so we also explain how does Protobuf 
wraps around the messages. 

##### OSGi only?
Throughout the following examples we are focusing on OSGi bundle deployments descriptors but the underlying classes, and hence the beans, can 
be used on whatever runtime as long as Spring Kafka and/or Spring Beans is supported (for example [Quarkus](https://quarkus.io/) 
or [Spring Boot](https://spring.io/projects/spring-boot)).

##### Ortogonal subjects
Please avoid being distracted with the `type-converters` elements in the examples. Their existence is explained in the end of this page, and is included to
be able to convey a fully working example on a Karaf/OSGi environment.

##### Use cases
The following approaches are applicable if you just need to send a "basic" message to Kafka or receive "basic" async messages or if you have more complex strategies to enforce while producing
or consuming messages, like for example controlling the topics, the partitions, retry messages consuming, commit consumer offsets, etc.
 
If your use-case involves creating listeners for messages in real-time, the topic that you listen is not fixed or any other listening property is not well known take a look at the
more complex examples towards the end of this page and in particular to the way the listener factories are used. 

## Simple Producer and Consumer example

The following examples should be enough if your needs fit on the "basic" use case.

##### Producer
A `BaseMessageProducer` bean can be used to produce messages just by defining a bean for that class. That way the usage of 
the Kafka Template in our code is blackboxed. To use it declare a bean like this:
```xml
<bean id="mySimpleKafkaProducerBean" class="org.hitachivantara.spring.kafka.producer.BaseMessageProducer">
  <property name="kafkaTemplate" ref="kafkaProxyProducerKafkaTemplate"/>
  <property name="kafkaTopic" value="myKafkaTopic"/>
</bean>
```

The above `kafkaProxyProducerKafkaTemplate` is defined like this:
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

##### Consumer

To be able to consume messages we must declare a bean that extends the [AbstractMessageListener.java](https://github.com/pentaho/pentaho-osgi-bundles/blob/54c4e1d0ad1324962714d5c405318331b4f223f4/spring-kafka/api/src/main/java/org/hitachivantara/spring/kafka/listener/AbstractMessageListener.java).
We must supply that bean with this information:
* A consumer factory - we default this to the existing `org.springframework.kafka.core.DefaultKafkaConsumerFactory` class
* A Kafka topic name
* A Kafka group ID for the consumer 

```xml
<type-converters>
  <bean class="org.hitachivantara.osgi.service.blueprint.GenericsTypeConverter">
    <argument>
      <map key-type="java.lang.Class" value-type="java.lang.Class">
        <entry key="org.springframework.kafka.core.DefaultKafkaConsumerFactory" value="org.springframework.kafka.core.ConsumerFactory" />
        <entry key="org.apache.kafka.common.serialization.StringDeserializer" value="org.apache.kafka.common.serialization.Deserializer" />
        <entry key="org.hitachivantara.spring.kafka.protobuf.KafkaProtobufDeserializer" value="org.apache.kafka.common.serialization.Deserializer" />
        <entry key="com.hitachivantara.protobuf.api.proto.SimpleMessage$1" value="com.google.protobuf.Parser" />
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
```

The most simple consumer is just a basic class where we extend the [AbstractMessageListener.java](https://github.com/pentaho/pentaho-osgi-bundles/blob/54c4e1d0ad1324962714d5c405318331b4f223f4/spring-kafka/api/src/main/java/org/hitachivantara/spring/kafka/listener/AbstractMessageListener.java).
That class implementation can be as this:

```java
public class SimpleListener extends AbstractMessageListener<String, SimpleMessage> {
    
  // If we need custom logic on the topic name we can override this method
  @Override public String getTopic() {
    return "CustomTopic";
  }

  // If we need custom logic on the group ID we can override this method
  @Override public String getGroupId() {
    return "CustomGroupID";
  }

  @Override
  public void onMessage( ConsumerRecord<String, SimpleMessage> record, Acknowledgment acknowledgment ) {
    // DO SOMETHING WITH THE MESSAGE
    try {
      
    } finally {
      // ACK the message
      acknowledgment.acknowledge();
    }
  }
}
```

This above class when declared in the blueprint as above will create a message consumer for Kafka messages sent to the `SimpleTopic` 
in the group `SimpleConsumerGroupId` (both topic and group can be further customized as needed like is shown above).

The message, and consequently the consumer offset, will only be committed after the message is acknowledge (it's possible to
have a consumer that auto acknowledge messages by using the `org.springframework.kafka.listener.MessageListener` implementation instead of our ).

It's important to notice that the bean definition has `init-method="start" destroy-method="stop"` defined, which mean that the listener will be
started when our blueprint bean is initiated and stopped when it is destroyed (refer to the 
blueprint [lifecycle callbacks](https://docs.osgi.org/specification/osgi.cmpn/7.0.0/service.blueprint.html#i2339865) for more information).

## Messages Spec (Protobuf)

We make extended use of Protobuf messages to send message with Kafka. To be able to use Protobuf `.proto` files with the message spec must be authored
and compiled. In the parent poms a profile is activated when a folder named `proto` exists in a module source. Refer to the [parent poms 
](https://github.com/pentaho/maven-parent-poms/blob/6692f5d4894d65457259f9cec1f7d37ee8ce6d40/pom.xml#L1986) to better understand how the `.proto` files are assembled into Java code.

### Producer Key & Value Serializer
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

## Advanced Producer and Consumer

##### Producer
It's possible to have a more complex producer for Kafka messages by using the Kafka Template directly inside our application beans:
```xml
  <bean id="ourApplicationBean" class="com.hitachivantara.ApplicationBean">
    <property name="kafkaTemplate" ref="kafkaProxyProducerKafkaTemplate"/> <!-- type converter used on this argument -->
  </bean>
``` 

The [Kafka Template API](https://docs.spring.io/spring-kafka/docs/1.3.10.RELEASE/api/index.html?org/springframework/kafka/core/KafkaTemplate.html) can 
be fully accessed this way and not restricted to what is exposed on the simple `BaseMessageProducer`. 
```java
public class ApplicationBean {
  private KafkaTemplate<String, Message> kafkaTemplate;
  
  public void setKafkaTemplate( KafkaTemplate<String, Message> kafkaTemplate ) {
    this.kafkaTemplate = kafkaTemplate;
  }
  
  public void someBeanMethod() {
    // use the kafkaTemplate to send messages
  }
}
```

##### Consumer

The simple consumer is fine if we have simple requirements when consuming messages. But more complex approaches may require for example retry policies.

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

<!-- Complex Listener -->
<bean id="kafkaComplexMessageListenerFactory" class="com.hitachivantara.kafka.ComplexListener">
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

The retry listener can use four properties to manage how the message is consumed when retrying.
* `retryMaxAttempts` - the number of attempts of re-processing an inbound message (default: 3)
* `backOffInitialInterval` - the back-off initial interval on retry (default: 1000)
* `backOffIMaxInterval` - the maximum back-off interval (default: 10000)
* `backOffIMultiplier` - the back-off multiplier (default: 2.0)
Using the default values we ensure that the message will be retied 3 times. After the first failure there will be a 1000ms delay before
retrying again, and after that we will multiply by 2.0 that delay for each retry (1000, then 2000, then 4000). The delay between retries
will never exceed the 10000ms.

```java
public class ComplexListener extends AbstractRetryMessageListener<String, ComplexMessage> {
  
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

Notice that on the above implementation we are not acknowledging the message in the finally block because the message can be retried. If
a `RuntimeException` is thrown the ACK won't be issued. 

## Consumer Factories

We can make use of factories to abstract some properties from the static bean definitions whenever we don't know beforehand what is the full 
configuration for the consumer or if we need to dynamically create new consumer listeners.

To implement this pattern the bean that we defined is a factory for a `org.hitachivantara.spring.kafka.listener.AbstractRetryMessageListener`
instances (a simple `AbstractMessageListenerFactory` also exists).
That factory follows a vanilla way to implement a factory pattern where some of the final bean (`AbstractRetryMessageListener`) properties
are initialized, and some run time properties (the topic and group ID) are received in runtime when new instances are created.

The factory is a generic type object which means that it will work with any implementation of the `AbstractRetryMessageListener` class:
```xml
<bean id="kafkaComplexMessageListenerFactory" class="com.hitachivantara.kafka.ComplexListenerFactory">
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

## KafkaTemplate
We use a [Kafka Template](https://docs.spring.io/spring-kafka/docs/1.3.11.RELEASE/reference/html/_reference.html#kafka-template) to 
send messages using it's API defined in [`org.springframework.kafka.core.KafkaTemplate`](https://docs.spring.io/spring-kafka/docs/1.3.10.RELEASE/api/index.html?org/springframework/kafka/core/KafkaTemplate.html).

The paradigm behind the Kafka Template is to have a simple wrapper around a producer with simple methods to send messages to Kafka topics.

That class has two constructors. Both accept as the first argument an interface `org.apache.kafka.clients.producer.Producer` implementation instance
and the second optional argument is a `boolean` to configure the auto flush ability of the Kafka Template instance.

## Default producer factory
Spring Kafka provides a default implementation for that interface `org.springframework.kafka.core.DefaultKafkaProducerFactory`. We
default to use that, for which we must provide an `HashMap` with the Kafka connection and library properties.

Those properties are defined in this example in the `kafkaProducerProperties`, and notice that only the basic ones are being used.
Kafka and Spring Kafka documentation should be used to check which properties exist and how they may influence the application.

## Type Converter

Through out the above examples we used [`type-converters`](https://docs.osgi.org/specification/osgi.cmpn/7.0.0/service.blueprint.html#i2709370) elements 
which are needed because the type erasure when injecting beans in Karaf is not following the same algorithm as Java, and so methods which 
accept Generic types don't recognize the existing methods as valid ones to use in the injection.

Notice that this element must be declared on the top of the blueprint. Further info can be seen in the `osgi-blueprint-api` module
class [GenericsTypeConverter.java](https://github.com/pentaho/pentaho-osgi-bundles/blob/54c4e1d0ad1324962714d5c405318331b4f223f4/osgi-blueprint-api/src/main/java/org/hitachivantara/osgi/service/blueprint/GenericsTypeConverter.java)
javadoc.

See also below issues in apache Aries project to more details:
[Apache ARIES-1500](https://issues.apache.org/jira/browse/ARIES-1500)
[Apache ARIES-1607](https://issues.apache.org/jira/browse/ARIES-1607)
