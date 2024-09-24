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
