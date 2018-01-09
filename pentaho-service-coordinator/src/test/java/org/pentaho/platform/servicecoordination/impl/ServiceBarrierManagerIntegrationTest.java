/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.platform.servicecoordination.impl;

import aQute.bnd.osgi.Constants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
//import org.pentaho.platform.api.engine.IServiceBarrier;
//import org.pentaho.platform.api.engine.IServiceBarrierManager;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;
import static org.ops4j.pax.tinybundles.core.TinyBundles.withBnd;

//@RunWith(PaxExam.class)
//@ExamReactorStrategy(PerMethod.class)
public class ServiceBarrierManagerIntegrationTest {


/*  @Configuration
  public Option[] config() throws FileNotFoundException {

    InputStream inp = bundle()
        .add( IServiceBarrier.class )
        .add( IServiceBarrierManager.class )
        .add( PhaserServiceBarrier.class )
        .add( ServiceBarrierManager.class )
        .set( Constants.EXPORT_PACKAGE, "*" )
        .set( Constants.IMPORT_PACKAGE, "*" )
        .add( "/OSGI-INF/blueprint/beans.xml",
            new FileInputStream( new File( "src/main/resources/OSGI-INF/blueprint/beans.xml" ) ) )
        .build( withBnd() );


    final String projectVersion = "6.0-SNAPSHOT";



    MavenArtifactUrlReference karafUrl = maven()
        .groupId( "org.apache.karaf" )
        .artifactId( "apache-karaf" )
        .version( "3.0.2" )
        .type( "tar.gz" );

    return options(

        KarafDistributionOption.karafDistributionConfiguration()
            .frameworkUrl( karafUrl )
            .unpackDirectory( new File( "target/exam" ) )
            .useDeployFolder( false ),

//        vmOption( "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005" ),

        junitBundles(),
        provision( inp )
    );
  }


  @Inject
  private IServiceBarrierManager barrierManager;

  @Test
  public void testBarrierManager() {
    assertNotNull( barrierManager );

    IServiceBarrier barrier1 = barrierManager.getServiceBarrier( "test" );
    IServiceBarrier barrier2 = barrierManager.getServiceBarrier( "test" );
    IServiceBarrier barrier3 = barrierManager.getServiceBarrier( "testagain" );
    assertSame( barrier1, barrier2 );
    assertNotSame( barrier1, barrier3 );

  }*/
}
