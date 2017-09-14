/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
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
