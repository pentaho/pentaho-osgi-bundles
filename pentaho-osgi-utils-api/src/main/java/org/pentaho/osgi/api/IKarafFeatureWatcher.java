package org.pentaho.osgi.api;

/**
 * Interface defining a class which serves one purpose, block until all features defined in the Karaf featuresBoot are
 * installed.
 * <p/>
 * Created by nbaker on 2/19/15.
 */
public interface IKarafFeatureWatcher {
  void waitForFeatures() throws FeatureWatcherException;


  class FeatureWatcherException extends Exception {
    public FeatureWatcherException( String message ) {
      super( message );
    }

    public FeatureWatcherException( String message, Throwable cause ) {
      super( message, cause );
    }
  }
}
