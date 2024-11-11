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

package org.hitachivantara.process;

import java.util.concurrent.CompletableFuture;


/**
 * ProcessGovernor implementations support managed execution of external processes.
 * <p>
 * Unlike {@link java.lang.ProcessBuilder}, a ProcessGovernor limits the maximum number of concurrent
 * processes.
 */
public interface ProcessGovernor {

  /**
   * Starts command under the ProcessGovernors management.  This memthod returns a CompletableFuture
   * that will eventually hold the running process, at whatever point the ProcessGovernor determines
   * it can be started.
   */
  CompletableFuture<Process> start( String... command );

  /**
   * Returns the number of process slots available.
   * TODO:  consider passing back a class with more info instead-- e.g. process list, start time, stdin/stdout, etc.
   */
  int availablePermits();

}
