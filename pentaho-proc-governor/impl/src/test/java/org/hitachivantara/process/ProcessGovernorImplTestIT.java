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

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

public class ProcessGovernorImplTestIT {

  private ExecutorService execSvc = Executors.newCachedThreadPool();
  private ProcessGovernor myService = new ProcessGovernorImpl( execSvc, 1 );

  private static final String LINUX_SLEEP = "sleep";
  private static final String WINDOWS_SLEEP = "timeout";

  private static final int MAX_WAIT_SECONDS = 8;

  private String sleep = System.getProperty( "os.name" ).toLowerCase()
    .startsWith( "windows" ) ? WINDOWS_SLEEP : LINUX_SLEEP;

  @Test
  public void commandsStartedConcurrentlyWith1PermitRunOneAtATime()
    throws InterruptedException, ExecutionException, TimeoutException {
    CompletableFuture<Process> futureProc = myService.start( sleep, "10" );
    CompletableFuture<Process> futureProc2 = myService.start( sleep, "10" );

    Process proc = futureProc.get( MAX_WAIT_SECONDS, SECONDS );
    assertThat( myService.availablePermits(), equalTo( 0 ) );

    shouldTimeout( () -> futureProc2.get( 1, MILLISECONDS ) );

    assertThat( proc.waitFor( MAX_WAIT_SECONDS, MILLISECONDS ), equalTo( false ) );
    futureProc.cancel( true );
    futureProc2.cancel( true );
  }

  @Test
  public void commandsStartedConcurrent2yWith2PermitRunTwoAtATime()
    throws InterruptedException, ExecutionException, TimeoutException {
    // set 2 permits
    myService = new ProcessGovernorImpl( execSvc, 2 );

    CompletableFuture<Process> futureProc = myService.start( sleep, "10" );
    CompletableFuture<Process> futureProc2 = myService.start( sleep, "10" );

    Process proc = futureProc.get( MAX_WAIT_SECONDS, SECONDS );
    Process proc2 = futureProc2.get( MAX_WAIT_SECONDS, SECONDS );

    assertThat( myService.availablePermits(), equalTo( 0 ) );

    //shouldTimeout( () -> futureProc2.get( 1, MILLISECONDS ) );

    assertThat( proc.waitFor( 1, MILLISECONDS ), equalTo( false ) );
    assertThat( proc2.waitFor( 1, MILLISECONDS ), equalTo( false ) );

    futureProc.cancel( true );
    futureProc2.cancel( true );
  }

  private void shouldTimeout( Callable callable ) {
    try {
      callable.call();
      fail();
    } catch ( Exception te ) {
      assertThat( te, instanceOf( TimeoutException.class ) );
    }
  }

}
