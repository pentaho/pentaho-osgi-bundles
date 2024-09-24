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
package org.pentaho.osgi.manager.resource.api;

import org.osgi.framework.Bundle;

/**
 * Created by krivera on 6/22/17.
 */
public interface ResourceHandler {

  /**
   * Determines whether the provided bundle has managed resources to be extracted
   *
   * @param bundle - The current blueprint bundle
   * @return {@link Boolean} if the bundle has managed resources
   */
  public boolean hasManagedResources( Bundle bundle );

  /**
   * Provided a {@link Bundle} this method handles its resources
   *
   * @param blueprintBundle The {@link Bundle} to handle its resources
   */
  public void handleManagedResources( Bundle blueprintBundle );
}
