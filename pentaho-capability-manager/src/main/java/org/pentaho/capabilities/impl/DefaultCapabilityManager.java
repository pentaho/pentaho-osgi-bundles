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

import org.pentaho.capabilities.api.ICapability;
import org.pentaho.capabilities.api.ICapabilityManager;
import org.pentaho.capabilities.api.ICapabilityProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by nbaker on 4/6/15.
 */
public class DefaultCapabilityManager implements ICapabilityManager {

  public static final String ID = "default";
  private Map<String, ICapabilityProvider> providers = new HashMap<String, ICapabilityProvider>(  );

  private static DefaultCapabilityManager INSTANCE = new DefaultCapabilityManager();
  protected DefaultCapabilityManager(){

  }

  public static DefaultCapabilityManager getInstance(){
    return INSTANCE;
  }

  public void registerCapabilityProvider( ICapabilityProvider provider ){
    providers.put( provider.getId(), provider );
  }

  @Override public Set<String> listProviders() {
    return providers.keySet();
  }

  @Override public ICapabilityProvider getProvider( String id ) {
    return providers.get( id );
  }

  @Override public String getId() {
    return ID;
  }

  @Override public Set<String> listCapabilities() {
    Set<String> capabilities = new HashSet<String>();
    for ( ICapabilityProvider iCapabilityProvider : providers.values() ) {
      Set<String> caps = iCapabilityProvider.listCapabilities();
      if( caps != null ) {
        capabilities.addAll( caps );
      }
    }
    return capabilities;
  }

  @Override public ICapability getCapabilityById( String id ){
    for ( ICapabilityProvider iCapabilityProvider : providers.values() ) {
      ICapability capabilityById = iCapabilityProvider.getCapabilityById( id );
      if( capabilityById != null ) {
        return capabilityById;
      }
    }
    return null;
  }

  @Override public Set<ICapability> getAllCapabilities() {
    Set<ICapability> capabilities = new HashSet<ICapability>();
    for ( ICapabilityProvider iCapabilityProvider : providers.values() ) {
      Set<ICapability> caps = iCapabilityProvider.getAllCapabilities();
      if( caps != null ) {
        capabilities.addAll( caps );
      }
    }
    return capabilities;
  }
}
