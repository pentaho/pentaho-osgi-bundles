# Hitachi Vantara Object Tunnel
The module supplies classes to support custom de/serialization on top of raw Object Streams.

# TunnelOutput
Written Objects are serialized to string by configured Serializatoin Functions. The 
result is wrapped in a TunneledObject class which is then written to the raw 
ObjectOutputStream.

Example:
```java
    ObjectOutputStream outputStream = new ObjectOutputStream( ... );
    TunnelOutput output = new TunnelOutput( outputStream );
    
    output.setSerializeFunctions( Collections.singletonMap( "UUID", o -> {
      return o.toString();
    }) );
 
    UUID uuid = UUID.randomUUID();
    output.writeObject( "UUID", uuid );

```

# TunnleInput
The TunnelInput class wraps an ObjectInputStream and reads off the TunneledPayload objects. 
The serialized content is passed to configured Deserialize functions. The 
resulting deserialized objects are broadcast to subscribers of the Input.

Example:
```java

    ObjectInputStream objectInputStream = new ObjectInputStream( ... );
    
    TunnelInput tunnel = new TunnelInput( objectInputStream );
    tunnel.setDeserializeFunctions( Collections.singletonMap( "type", UUID::fromString ) );

    tunnel.subscribe( new SubscriberAdapter<TunneledInputObject>() {
      @Override public void onNext( TunneledInputObject tunneledObject ) {
        tunnelObject.getType();
        tunnelObject.getObject();
      }
    } );
    tunnel.open();
```
