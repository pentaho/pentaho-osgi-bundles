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

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyRegistration;

import java.util.Collections;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Created by nbaker on 8/14/15.
 */
public class ProxyRequestTrackerTest {

  @Test
  public void testProxyRequestTracker(){

    PentahoSystem.clearObjectFactory();

    ProxyFactoryImpl proxyFactory = new ProxyFactoryImpl( null );
    IProxyCreator<String> creator = mock( IProxyCreator.class );
    IProxyCreator<String> creator2 = mock( IProxyCreator.class );
    when( creator.supports( String.class )).thenReturn( true );
    when( creator2.supports( Integer.class )).thenReturn( true );

    String target = "Hello World";
    Integer target2 = 123;
    when( creator.create( target ) ).thenReturn( "Good Night" );
    when( creator2.create( target2 ) ).thenThrow( new RuntimeException( "foobar" ) );

    proxyFactory.setCreators( Collections.<IProxyCreator<?>>singletonList( creator ) );

    BundleContext bundleContext = mock( BundleContext.class );
    ProxyRequestTracker tracker = new ProxyRequestTracker( bundleContext, proxyFactory );

    //add
    ProxyRequestRegistration registration = new ProxyRequestRegistration( String.class );
    tracker.registrationAdded( registration );
    assertTrue( tracker.getClassesToTrack().containsKey( String.class ) );

    //remove
    tracker.registrationRemoved( registration );
    assertFalse( tracker.getClassesToTrack().containsKey( String.class ) );
    tracker.registrationRemoved( null ); // should be silent, no exception

    //add
    tracker.registrationAdded( registration );
    ProxyRequestTracker.ProxyTargetServiceTracker serviceTracker =
        tracker.getClassesToTrack().get( String.class );
    ServiceReference<String> reference = mock( ServiceReference.class );
    ServiceReference<Integer> reference2 = mock( ServiceReference.class );
    String key1 = "key1";
    when(reference.getPropertyKeys()).thenReturn( new String[] { key1 } );
    when( reference.getProperty( key1 )).thenReturn( "value1" );

    when(reference2.getPropertyKeys()).thenReturn( new String[] { key1 } );
    when( reference2.getProperty( key1 )).thenReturn( "value1" );


    when( bundleContext.getService( reference )).thenReturn( target );
    when( bundleContext.getService( reference2 )).thenReturn( target2 );
    serviceTracker.addingService( reference );
    verify( creator, times( 1 ) ).create( target );

    String s = PentahoSystem.get( String.class );
    assertEquals( "Good Night", s );

    serviceTracker.removedService( reference, target );
    assertNull( PentahoSystem.get( String.class ) );


    serviceTracker.addingService( reference );
    serviceTracker.modifiedService( reference, target );
    s = PentahoSystem.get( String.class );
    assertEquals( "Good Night", s );

    serviceTracker.addingService( reference2 );

  }
}
