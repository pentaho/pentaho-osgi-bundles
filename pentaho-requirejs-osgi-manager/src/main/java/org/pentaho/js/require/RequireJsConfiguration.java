/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.js.require;

import org.osgi.framework.Bundle;

import java.util.List;

/**
 * Created by bryan on 9/2/14.
 */
public class RequireJsConfiguration {
  private final Bundle bundle;
  private final List<String> requireConfigurations;

  public RequireJsConfiguration( Bundle bundle, List<String> requireConfigurations ) {
    this.bundle = bundle;
    this.requireConfigurations = requireConfigurations;
  }

  public Bundle getBundle() {
    return bundle;
  }

  public List<String> getRequireConfigurations() {
    return requireConfigurations;
  }
}
