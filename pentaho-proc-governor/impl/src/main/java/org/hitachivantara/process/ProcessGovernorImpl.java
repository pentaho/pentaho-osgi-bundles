/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2019 Hitachi Vantara. All rights reserved.
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

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

import static java.lang.String.format;

/**
 * A ProcessGovernor implementation which launches separate threads for each proc execution request,
 * used for managing proc execution and completion.
 * <p>
 * <p>
 */
public class ProcessGovernorImpl implements ProcessGovernor {
  private static final Logger logger = LoggerFactory.getLogger( ProcessGovernorImpl.class );
  private final int maxProcesses;
  private final ExecutorService execService;
  private final Semaphore semaphore;

  @SuppressWarnings ( "WeakerAccess" )
  public ProcessGovernorImpl( ExecutorService execService, int maxProcesses ) {
    this.execService = execService;
    semaphore = new Semaphore( maxProcesses );
    this.maxProcesses = maxProcesses;
  }

  /**
   * Executes command if a permit is available.  Returns a future that provides
   * a handle to the running process, once it has started.  The process may
   * be delayed until a permit becomes available.
   *
   * @param command the command to be executed in a separate process
   * @return completable future for the process.
   */
  @SuppressWarnings ( "FutureReturnValueIgnored" )
  @Override public synchronized CompletableFuture<Process> start( String... command ) {
    if ( logger.isDebugEnabled() ) {
      Preconditions.checkState( semaphore.availablePermits() <= maxProcesses,
        "Number of permits should never exceed the starting maxProcesses" );
      logger.debug( format( "Submitting command for execution [%s]", Arrays.toString( command ) ) );
    }

    CompletableFuture<Process> futureProc = new CompletableFuture<>();
    execService.submit( startProcess( command, futureProc ) );

    return futureProc;
  }

  @Override public int availablePermits() {
    return semaphore.availablePermits();
  }

  private Runnable startProcess( String[] command, CompletableFuture<Process> futureProc ) {
    return () -> {
      Optional<Integer> exitValue = Optional.empty();
      try {
        semaphore.acquire();
        if ( logger.isDebugEnabled() ) {
          logger.debug( String.format( "Executing command %s", Arrays.toString( command ) ) );
        }
        Process proc = getProcess( command );
        futureProc.complete( proc );

        exitValue = Optional.of( getExitValue( proc ) );

      } catch ( IOException e ) {
        logger.error( e.getMessage(), e );
        futureProc.completeExceptionally( e );
      } catch ( InterruptedException e ) {
        Thread.currentThread().interrupt();
        logger.error( e.getMessage(), e );
        futureProc.completeExceptionally( e );
      } finally {
        semaphore.release();
        String returnCode = exitValue
          .map( Object::toString )
          .orElse( "No exit value. " );
        if ( logger.isDebugEnabled() ) {
          logger.debug( format( "Command complete [%s]%nExit value [%s]%nemaphores available=%s",
            Arrays.toString( command ), returnCode, availablePermits() ) );
        }
      }
    };
  }

  private Process getProcess( String[] command ) throws IOException {
    ProcessBuilder builder = new ProcessBuilder( command );

    return builder.start();
  }

  private Integer getExitValue( Process proc ) {
    try {
      proc.waitFor();
    } catch ( InterruptedException e ) {
      Thread.currentThread().interrupt();
      logger.error( "Interrupted while waiting for proc completion." );
    }
    return proc.exitValue();
  }

}
