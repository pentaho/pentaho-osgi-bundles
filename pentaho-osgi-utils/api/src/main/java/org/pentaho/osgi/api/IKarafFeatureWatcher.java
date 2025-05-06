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

package org.pentaho.osgi.api;

import java.io.IOException;
import java.util.List;

/**
 * Interface defining a class which serves one purpose, block until all features defined in the Karaf featuresBoot are
 * installed.
 * <p/>
 * Created by nbaker on 2/19/15.
 */
public interface IKarafFeatureWatcher {
  void waitForFeatures() throws FeatureWatcherException;

  List<String> getFeatures( String configPersistentId, String featuresPropertyKey ) throws IOException;

  class FeatureWatcherException extends Exception {
    public FeatureWatcherException( String message ) {
      super( message );
    }

    public FeatureWatcherException( String message, Throwable cause ) {
      super( message, cause );
    }
  }
}
