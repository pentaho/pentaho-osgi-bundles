package org.pentaho.capabilities.api;

import java.util.Set;

/**
 * Created by nbaker on 4/6/15.
 */
public interface ICapabilityManager extends ICapabilityProvider{
  Set<String> listProviders();
  ICapabilityProvider getProvider( String id );
}
