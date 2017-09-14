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
