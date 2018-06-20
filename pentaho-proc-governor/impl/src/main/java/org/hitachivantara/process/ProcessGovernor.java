/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2018 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */
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
