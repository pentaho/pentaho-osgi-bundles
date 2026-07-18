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


package org.pentaho.webpackage.core;

import java.util.Map;

public interface IPentahoWebPackage {
  String getName();

  String getVersion();

  String getResourceRootPath();

  String getWebRootPath();

  Map<String, Object> getPackageJson();
}
