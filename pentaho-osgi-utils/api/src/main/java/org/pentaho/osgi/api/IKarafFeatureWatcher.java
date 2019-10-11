/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
