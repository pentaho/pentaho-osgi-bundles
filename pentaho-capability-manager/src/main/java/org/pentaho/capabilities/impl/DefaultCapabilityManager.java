/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.capabilities.impl;

import org.pentaho.capabilities.api.ICapability;
import org.pentaho.capabilities.api.ICapabilityManager;
import org.pentaho.capabilities.api.ICapabilityProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by nbaker on 4/6/15.
 */
public class DefaultCapabilityManager implements ICapabilityManager {

  public static final String ID = "default";
  private Map<String, ICapabilityProvider> providers = new HashMap<String, ICapabilityProvider>();

  private static DefaultCapabilityManager INSTANCE = new DefaultCapabilityManager();

  protected DefaultCapabilityManager() {

  }

  public static DefaultCapabilityManager getInstance() {
    return INSTANCE;
  }

  public void registerCapabilityProvider( ICapabilityProvider provider ) {
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
      if ( caps != null ) {
        capabilities.addAll( caps );
      }
    }
    return capabilities;
  }

  @Override public ICapability getCapabilityById( String id ) {
    for ( ICapabilityProvider iCapabilityProvider : providers.values() ) {
      ICapability capabilityById = iCapabilityProvider.getCapabilityById( id );
      if ( capabilityById != null ) {
        return capabilityById;
      }
    }
    return null;
  }

  @Override public boolean capabilityExist( String id ) {
    for ( ICapabilityProvider iCapabilityProvider : providers.values() ) {
      if ( iCapabilityProvider.capabilityExist( id ) ) {
        return true;
      }
    }
    return false;
  }

  @Override public Set<ICapability> getAllCapabilities() {
    Set<ICapability> capabilities = new HashSet<ICapability>();
    for ( ICapabilityProvider iCapabilityProvider : providers.values() ) {
      Set<ICapability> caps = iCapabilityProvider.getAllCapabilities();
      if ( caps != null ) {
        capabilities.addAll( caps );
      }
    }
    return capabilities;
  }
}
