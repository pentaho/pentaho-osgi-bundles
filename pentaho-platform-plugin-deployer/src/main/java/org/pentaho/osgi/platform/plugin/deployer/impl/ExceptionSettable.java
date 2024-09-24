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
package org.pentaho.osgi.platform.plugin.deployer.impl;

/**
 * Created by bryan on 8/28/14.
 */
public interface ExceptionSettable<T extends Throwable> {
  public void setException( T exception );
}
