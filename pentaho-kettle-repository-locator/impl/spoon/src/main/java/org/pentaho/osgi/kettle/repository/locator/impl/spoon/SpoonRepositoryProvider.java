/*!
 * Copyright 2010 - 2016 Pentaho Corporation.  All rights reserved.
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

package org.pentaho.osgi.kettle.repository.locator.impl.spoon;

import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.osgi.kettle.repository.locator.api.KettleRepositoryProvider;

import java.util.function.Supplier;

/**
 * Created by bryan on 4/15/16.
 */
public class SpoonRepositoryProvider implements KettleRepositoryProvider {
  private final Supplier<Spoon> spoonSupplier;

  public SpoonRepositoryProvider() {
    this( Spoon::getInstance );
  }

  public SpoonRepositoryProvider( Supplier<Spoon> spoonSupplier ) {
    this.spoonSupplier = spoonSupplier;
  }

  @Override public Repository getRepository() {
    return spoonSupplier.get().getRepository();
  }
}
