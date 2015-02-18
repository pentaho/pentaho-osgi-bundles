package org.pentaho.platform.servicecoordination.api;

/**
 * Created by nbaker on 1/27/15.
 */
public interface IPhasedLifecycleListener<T> {
  void onPhaseChange( IPhasedLifecycleEvent<T> event );
}
