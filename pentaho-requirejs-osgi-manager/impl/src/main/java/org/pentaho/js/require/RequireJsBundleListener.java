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
package org.pentaho.js.require;

import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * Created by bryan on 8/15/14.
 */
public class RequireJsBundleListener implements BundleListener {
  private final RequireJsConfigManager requireJsConfigManager;

  public RequireJsBundleListener( RequireJsConfigManager requireJsConfigManager ) {
    this.requireJsConfigManager = requireJsConfigManager;
  }

  @Override
  public void bundleChanged( BundleEvent event ) {
    switch ( event.getType() ) {
      case BundleEvent.STARTED:
      case BundleEvent.STOPPED:
        requireJsConfigManager.bundleChanged( event.getBundle() );
        break;
    }
  }
}
