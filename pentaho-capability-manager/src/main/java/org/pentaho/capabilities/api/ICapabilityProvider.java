package org.pentaho.capabilities.api;

import java.util.Set;

/**
 * Created by nbaker on 4/6/15.
 */
public interface ICapabilityProvider {
  String getId();
  Set<String> listCapabilities();
  ICapability getCapabilityById( String id );
  Set<ICapability> getAllCapabilities();
}
