/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.requirejs;

import java.util.Map;
import java.util.function.Function;

public interface IRequireJsPackageConfigurationPlugin {
  void apply( IRequireJsPackageConfiguration requireJsPackageConfig,
              Function<String, IRequireJsPackageConfiguration> dependencyResolver,
              Function<String, String> resolveModuleId,
              Map<String, ?> requireConfig );
}
