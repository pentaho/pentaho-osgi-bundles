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


package org.pentaho.requirejs;

import java.util.Map;
import java.util.function.Function;

public interface IRequireJsPackageConfigurationPlugin {
  void apply( IRequireJsPackageConfiguration requireJsPackageConfig,
              Function<String, IRequireJsPackageConfiguration> dependencyResolver,
              Function<String, String> resolveModuleId,
              Map<String, ?> requireConfig );
}
