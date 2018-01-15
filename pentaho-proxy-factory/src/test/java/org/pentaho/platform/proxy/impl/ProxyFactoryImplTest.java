/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.platform.proxy.impl;

import org.junit.Test;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyRegistration;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by nbaker on 8/13/15.
 */
public class ProxyFactoryImplTest {

  @Test
  public void testCreateProxyWithNoCreator() throws Exception {
    ProxyFactoryImpl proxyFactory = new ProxyFactoryImpl( null );
    try{
      proxyFactory.createAndRegisterProxy( "Hello World", Collections.<Class<?>>singletonList( CharSequence.class ),
          Collections.<String, Object>emptyMap() );
      fail( "Should have thrown a ProxyException" );
    } catch ( ProxyException e ){

    }
    try{
      proxyFactory.createProxy( "Hello World" );
      fail( "Should have thrown a ProxyException" );
    } catch ( ProxyException e ){

    }

  }
  @Test
  public void testCreateProxy() throws Exception {
    ProxyFactoryImpl proxyFactory = new ProxyFactoryImpl( null );
    IProxyCreator<String> creator = mock( IProxyCreator.class );
    when( creator.supports( String.class )).thenReturn( true );

    String target = "Hello World";
    when( creator.create( target ) ).thenReturn( "Good Night" );
    proxyFactory.setCreators( Collections.<IProxyCreator<?>>singletonList( creator ) );

    IProxyRegistration proxy = proxyFactory
        .createAndRegisterProxy( target, Collections.<Class<?>>singletonList( CharSequence.class ),
            Collections.<String, Object>singletonMap( "key", "master" ) );
    assertNotNull( proxy );

    CharSequence registeredString = PentahoSystem.get( CharSequence.class, null, Collections.singletonMap( "key", "master" ) );
    assertEquals( "Good Night", registeredString );

    // Test plain create
    String plainProxy = proxyFactory.createProxy( target );
    assertNotNull( proxy );
    assertEquals( "Good Night", plainProxy );

  }

  @Test
  public void testProxyRegistration() throws ProxyException {
    ProxyFactoryImpl proxyFactory = new ProxyFactoryImpl( null );
    IProxyCreator<String> creator = mock( IProxyCreator.class );
    when( creator.supports( String.class )).thenReturn( true );

    String target = "Hello World";
    when( creator.create( target ) ).thenReturn( "Good Night" );
    proxyFactory.setCreators( Collections.<IProxyCreator<?>>singletonList( creator ) );

    IProxyRegistration proxy = proxyFactory
        .createAndRegisterProxy( target, Collections.<Class<?>>singletonList( CharSequence.class ),
            Collections.<String, Object>singletonMap( "key", "master" ) );
    assertNotNull( proxy );
    assertEquals( "Good Night", proxy.getProxyObject() );


    // Found in PentahoSystem
    CharSequence registeredString = PentahoSystem.get( CharSequence.class, null,
        Collections.singletonMap( "key", "master" ) );
    assertEquals( "Good Night", registeredString );

    // De-register then make sure removed from PentahoSystem.
    proxy.getPentahoObjectRegistration().remove();
    registeredString = PentahoSystem.get( CharSequence.class, null,
        Collections.singletonMap( "key", "master" ) );
    assertNull( registeredString );
  }


  @Test
  public void testInheritenceScenarios() throws Exception {

    { // Interface
      ProxyFactoryImpl proxyFactory = new ProxyFactoryImpl( null );
      IProxyCreator<String> creator = mock( IProxyCreator.class );
      when( creator.supports( IAB.class ) ).thenReturn( true );
      AB ab = new AB();
      when( creator.create( ab ) ).thenReturn( "works" );
      proxyFactory.setCreators( Collections.<IProxyCreator<?>>singletonList( creator ) );

      IProxyRegistration proxy =
          proxyFactory.createAndRegisterProxy( ab, Collections.<Class<?>>singletonList( IA.class ), Collections.<String, Object>emptyMap() );
      assertEquals( "works", proxy.getProxyObject() );
    }

    { // Class
      ProxyFactoryImpl proxyFactory = new ProxyFactoryImpl( null );
      IProxyCreator<String> creator = mock( IProxyCreator.class );
      when( creator.supports( AB.class ) ).thenReturn( true );
      AB ab = new AB();
      when( creator.create( ab ) ).thenReturn( "works" );
      proxyFactory.setCreators( Collections.<IProxyCreator<?>>singletonList( creator ) );

      IProxyRegistration proxy =
          proxyFactory.createAndRegisterProxy( ab, Collections.<Class<?>>singletonList( IA.class ), Collections.<String, Object>emptyMap() );
      assertEquals( "works", proxy.getProxyObject() );
    }

    { // superclass
      ProxyFactoryImpl proxyFactory = new ProxyFactoryImpl( null );
      IProxyCreator<String> creator = mock( IProxyCreator.class );
      when( creator.supports( AB.class ) ).thenReturn( true );
      ABC ab = new ABC();
      when( creator.create( ab ) ).thenReturn( "works" );
      proxyFactory.setCreators( Collections.<IProxyCreator<?>>singletonList( creator ) );

      IProxyRegistration proxy =
          proxyFactory.createAndRegisterProxy( ab, Collections.<Class<?>>singletonList( IA.class ), Collections.<String, Object>emptyMap() );
      assertEquals( "works", proxy.getProxyObject() );
    }


    { // multiple interfaces
      ProxyFactoryImpl proxyFactory = new ProxyFactoryImpl( null );
      IProxyCreator<String> creator = mock( IProxyCreator.class );
      when( creator.supports( IB.class ) ).thenReturn( true );
      ABSeperated ab = new ABSeperated();
      when( creator.create( ab ) ).thenReturn( "works" );
      proxyFactory.setCreators( Collections.<IProxyCreator<?>>singletonList( creator ) );

      IProxyRegistration proxy =
          proxyFactory.createAndRegisterProxy( ab, Collections.<Class<?>>singletonList( IA.class ), Collections.<String, Object>emptyMap() );
      assertEquals( "works", proxy.getProxyObject() );
    }

  }
  private interface IA{}
  private interface IB{}
  private interface IAB extends IA, IB{}
  private class A implements IA{}
  private class B implements IB{}
  private class AB implements IAB{}
  private class ABC extends AB{}
  private class ABSeperated implements IA, IB {}
}