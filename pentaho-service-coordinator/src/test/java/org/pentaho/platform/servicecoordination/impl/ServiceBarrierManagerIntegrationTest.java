/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
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
