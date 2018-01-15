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

import org.apache.karaf.bundle.core.BundleState;

/**
 * Created by bmorrise on 9/3/15.
 */
public interface BlueprintStateService {
  Boolean hasBlueprint( long bundleId );
  Boolean isBlueprintLoaded( long bundleId );
  Boolean isBlueprintTryingToLoad( long bundleId );
  Boolean isBlueprintFailed( long bundleId );
  BundleState getBundleState( long bundleId );
  String[] getBundleMissDependencies( long bundleId );
  Throwable getBundleFailureCause( long bundleId );
}
