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

import org.osgi.framework.Bundle;

/**
 * User: nbaker
 * Date: 12/17/10
 */
public interface BeanFactoryLocator {

  /**
   * Lookup Blueprint container for given bnudle.
   *
   * @param bundle
   * @return
   */
  BeanFactory getBeanFactory( Bundle bundle );

  /**
   * The service object might already by a blueprintContainer. Return a factory with it if so.
   *
   * @param serviceObject
   * @return
   */
  BeanFactory getBeanFactory( Object serviceObject );
}
