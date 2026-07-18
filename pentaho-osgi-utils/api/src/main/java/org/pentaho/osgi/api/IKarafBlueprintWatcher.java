/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.osgi.api;

/**
 * Created by bmorrise on 9/4/15.
 */
public interface IKarafBlueprintWatcher {
  void waitForBlueprint() throws BlueprintWatcherException;


  class BlueprintWatcherException extends Exception {
    public BlueprintWatcherException( String message ) {
      super( message );
    }

    public BlueprintWatcherException( String message, Throwable cause ) {
      super( message, cause );
    }
  }
}
