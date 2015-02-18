package org.pentaho.platform.servicecoordination.api;

/**
 * The interface for events sent by the IPhaseLifecycleManager. It supports access to the lifecycle phase and object
 * associated with that phase.
 *
 * Listeners receiving this event must call accept for the lifecycle to be considered complete. Failure to do so will
 * block the ability to move the phase in the IPhaseLifecycleManager.
 *
 * Created by nbaker on 1/27/15.
 */
public interface IPhasedLifecycleEvent<T> {
  int getPhase();
  T getNotificationObject();
  void accept();
  void exception( Throwable t );
}
