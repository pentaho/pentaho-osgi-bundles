/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.capabilities.impl;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.capabilities.api.ICapability;
import org.pentaho.capabilities.api.ICapabilityProvider;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by nbaker on 4/10/15.
 */
public class DefaultCapabilityManagerTest {

  DefaultCapabilityManager manager = new DefaultCapabilityManager();

  @Before
  public void setup(){
    manager = new DefaultCapabilityManager();
  }

  @Test
  public void testRegisterCapabilityProvider() throws Exception {
    Set<String> providers = manager.listProviders();
    assertEquals( 0, providers.size() );

    ICapabilityProvider iCapabilityProvider = mock( ICapabilityProvider.class );
    when(iCapabilityProvider.getId()).thenReturn( "test" );
    manager.registerCapabilityProvider( iCapabilityProvider );

    providers = manager.listProviders();
    assertEquals( 1, providers.size() );
    assertEquals( iCapabilityProvider.getId(), providers.iterator().next() );

  }

  @Test
  public void testGetProvider() throws Exception {
    ICapabilityProvider iCapabilityProvider = mock( ICapabilityProvider.class );
    when(iCapabilityProvider.getId()).thenReturn( "test" );
    manager.registerCapabilityProvider( iCapabilityProvider );

    ICapabilityProvider provider = manager.getProvider( "test" );
    assertSame( iCapabilityProvider, provider );
  }

  @Test
  public void testGetId() throws Exception {
    assertEquals( "default", manager.getId() );
  }

  @Test
  public void testListCapabilities() throws Exception {
    ICapabilityProvider iCapabilityProvider = mock( ICapabilityProvider.class );
    when(iCapabilityProvider.getId()).thenReturn( "test" );
    HashSet<String> inCapabilities = new HashSet<String>( Arrays.asList( "one", "two", "three" ) );
    when(iCapabilityProvider.listCapabilities()).thenReturn(
        inCapabilities );
    manager.registerCapabilityProvider( iCapabilityProvider );

    Set<String> capabilities = manager.listCapabilities();
    assertEquals(3, capabilities.size());
    assertTrue( Arrays.equals( capabilities.toArray(), inCapabilities.toArray() ));
  }

  @Test
  public void testGetCapabilityById() throws Exception {

    ICapabilityProvider iCapabilityProvider = mock( ICapabilityProvider.class );
    when(iCapabilityProvider.getId()).thenReturn( "test" );
    ICapability capability = mock( ICapability.class );
    when(iCapabilityProvider.getCapabilityById( "foo" )).thenReturn( capability );
    manager.registerCapabilityProvider( iCapabilityProvider );

    ICapability foo = manager.getCapabilityById( "foo" );
    assertSame( capability, foo );
  }

  @Test
  public void testCapabilityExist() throws Exception {

    ICapabilityProvider iCapabilityProvider = mock( ICapabilityProvider.class );
    when(iCapabilityProvider.capabilityExist("foo")).thenReturn( true);
    manager.registerCapabilityProvider( iCapabilityProvider );

    boolean exists = manager.capabilityExist( "foo" );
    assertSame( true, exists );

    boolean doesNotExist = manager.capabilityExist( "bad" );
    assertSame( false, doesNotExist );
  }

  @Test
  public void testGetAllCapabilities() throws Exception {

    ICapabilityProvider iCapabilityProvider = mock( ICapabilityProvider.class );
    when(iCapabilityProvider.getId()).thenReturn( "test" );
    ICapability capability = mock( ICapability.class );
    when(iCapabilityProvider.getAllCapabilities()).thenReturn( new HashSet<ICapability>( Arrays.asList( capability ) ) );
    manager.registerCapabilityProvider( iCapabilityProvider );

    Set<ICapability> allCapabilities = manager.getAllCapabilities();
    assertEquals( 1, allCapabilities.size() );

  }
}