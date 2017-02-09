/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.osgi.objecttunnel;

import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by nbaker on 2/6/17.
 */
public class TunneledInputTest {


  private TunnelInput tunnel;
  private ObjectOutputStream outputStream;
  private ByteArrayOutputStream byteArrayOutputStream;

  @Before
  public void setup() throws Exception {

    byteArrayOutputStream = new ByteArrayOutputStream();
    outputStream = new ObjectOutputStream( byteArrayOutputStream );

  }

  private void createTunnel() throws Exception {

    byte[] bytes = byteArrayOutputStream.toByteArray();
    ObjectInputStream objectInputStream = new ObjectInputStream( new ByteArrayInputStream( bytes ) );
    tunnel = new TunnelInput( objectInputStream );

  }

  @Test
  public void testTunnelInput() throws Exception {
    outputStream.writeObject( new TunneledPayload( "type", UUID.randomUUID().toString() ) );

    createTunnel();

    tunnel.setDeserializeFunctions( Collections.singletonMap( "type", UUID::fromString ) );

    CountDownLatch latch = new CountDownLatch( 1 );
    tunnel.subscribe( new SubscriberAdapter<TunneledInputObject>() {
      @Override public void onNext( TunneledInputObject tunneledObject ) {
        System.out.println( tunneledObject );
        latch.countDown();
      }
    } );
    tunnel.open();

    latch.await( 2, TimeUnit.SECONDS );
  }

  @Test
  public void testTunnelInputErrorThreshold() throws Exception {
    // Write 11 objects on the stream.
    for ( int i = 0; i < 11; i++ ) {
      outputStream.writeObject( new TunneledPayload( "type", UUID.randomUUID().toString() ) );
    }

    // Consume the output and create a tunnel on the input
    createTunnel();

    // setup factory that always throws an exception.
    tunnel.setDeserializeFunctions( Collections.singletonMap( "type", s -> {
      throw new IllegalStateException( "I am bad" );
    } ) );

    // The tunnel will continue until 10 consecutive errors
    tunnel.setErrorThreshold( 10 );

    CountDownLatch latch = new CountDownLatch( 1 );
    AtomicBoolean errored = new AtomicBoolean( false );

    // Our subscriber gets the 10 errors then triggers the latch to let the test complete.
    tunnel.subscribe( new SubscriberAdapter<TunneledInputObject>() {
      @Override public void onError( Throwable t ) {
        errored.set( true );
      }

      @Override public void onComplete() {
        latch.countDown();
      }
    } );
    tunnel.open();

    latch.await( 2, TimeUnit.SECONDS );

    assertTrue( errored.get() );
    assertEquals( 10, tunnel.getErrorCount() );

  }


  @Test
  public void testErrorCountReset() throws Exception {
    // Write 11 objects on the stream.
    for ( int i = 0; i < 10; i++ ) {
      outputStream.writeObject( new TunneledPayload( "UUID", UUID.randomUUID().toString() ) );
    }

    // Consume the output and create a tunnel on the input
    createTunnel();

    AtomicInteger count = new AtomicInteger( 0 );
    AtomicReference<String> uuid = new AtomicReference<>( null );
    // setup factory that throws an exception except for the 4th item.
    tunnel.setDeserializeFunctions( Collections.singletonMap( "UUID", s -> {
      count.incrementAndGet();
      if ( count.get() == 4 ) {
        uuid.set( s );
        return UUID.fromString( s );
      }
      throw new IllegalStateException( "I am bad" );
    } ) );

    // The tunnel will continue until 10 consecutive errors
    tunnel.setErrorThreshold( 10 );

    CountDownLatch latch = new CountDownLatch( 1 );
    AtomicBoolean errored = new AtomicBoolean( false );

    // Our subscriber gets the 10 errors then triggers the latch to let the test complete.
    tunnel.subscribe( new SubscriberAdapter<TunneledInputObject>() {
      @Override public void onNext( TunneledInputObject tunneledObject ) {
        assertEquals( "UUID", tunneledObject.getType() );
        assertEquals( uuid.get(), ( (UUID) tunneledObject.getObject() ).toString() );
      }

      @Override public void onError( Throwable t ) {
        errored.set( true );
      }

      @Override public void onComplete() {
        latch.countDown();
      }
    } );
    tunnel.open();

    latch.await( 30, TimeUnit.SECONDS );

    assertTrue( errored.get() );
    assertEquals( 10, tunnel.getErrorCount() );

  }


  private static class SubscriberAdapter<T> implements Subscriber<T> {
    @Override public void onSubscribe( Subscription s ) {

    }

    @Override public void onNext( T t ) {

    }

    @Override public void onError( Throwable t ) {

    }

    @Override public void onComplete() {

    }
  }


}