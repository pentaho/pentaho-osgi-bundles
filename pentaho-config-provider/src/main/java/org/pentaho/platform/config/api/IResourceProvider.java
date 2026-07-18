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

package org.pentaho.platform.config.api;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by nbaker on 8/8/15.
 */
public interface IResourceProvider {
  InputStream resolveResource( String resourcePath ) throws FileNotFoundException;
  boolean isInheriting();
}
