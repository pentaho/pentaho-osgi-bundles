/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
