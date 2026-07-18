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


package org.pentaho.osgi.platform.plugin.deployer.impl;

/**
 * Created by bryan on 8/28/14.
 */
public interface ExceptionSettable<T extends Throwable> {
  public void setException( T exception );
}
