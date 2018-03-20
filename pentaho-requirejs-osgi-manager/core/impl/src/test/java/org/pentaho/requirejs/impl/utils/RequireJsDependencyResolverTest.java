/*!
 * Copyright 2018 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.requirejs.impl.utils;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.requirejs.IRequireJsPackageConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class RequireJsDependencyResolverTest {
  private Collection<IRequireJsPackageConfiguration> packages;

  private IRequireJsPackageConfiguration mockPackageA_1_0_0;
  private IRequireJsPackageConfiguration mockPackageA_1_3_0;
  private IRequireJsPackageConfiguration mockPackageA_1_5_0;
  private IRequireJsPackageConfiguration mockPackageA_1_8_0;
  private IRequireJsPackageConfiguration mockPackageA_2_0_0;
  private IRequireJsPackageConfiguration mockPackageA_2_5_0;

  @Before
  public void setUp() {
    this.mockPackageA_1_0_0 = createMockPackage( "A", "1.0.0" );
    this.mockPackageA_1_3_0 = createMockPackage( "A", "1.3.0" );
    this.mockPackageA_1_5_0 = createMockPackage( "A", "1.5.0" );
    this.mockPackageA_1_8_0 = createMockPackage( "A", "1.8.0" );
    this.mockPackageA_2_0_0 = createMockPackage( "A", "2.0.0" );
    this.mockPackageA_2_5_0 = createMockPackage( "A", "2.5.0" );

    this.packages = new ArrayList<>();

    packages.add( this.mockPackageA_1_0_0 );
    packages.add( this.mockPackageA_1_3_0 );
    packages.add( this.mockPackageA_1_5_0 );
    packages.add( this.mockPackageA_1_8_0 );
    packages.add( this.mockPackageA_2_0_0 );
    packages.add( this.mockPackageA_2_5_0 );
  }

  private IRequireJsPackageConfiguration createMockPackage( String name, String version ) {
    return createMockPackage( name, version, null, null );
  }

  private IRequireJsPackageConfiguration createMockPackage( String name, String version, String dependencyName, String dependencyVersion ) {
    IRequireJsPackageConfiguration mockPackage = mock( IRequireJsPackageConfiguration.class );
    doReturn( name ).when( mockPackage ).getName();
    doReturn( version ).when( mockPackage ).getVersion();

    Map<String, String> dependencies = new HashMap<>();
    if ( dependencyName != null ) {
      dependencies.put( dependencyName, dependencyVersion );
    }
    doReturn( dependencies ).when( mockPackage ).getDependencies();

    return mockPackage;
  }

  @Test
  public void getResolvedVersionExactVersion() {
    packages.add( createMockPackage( "B", "1.0.0", "A", "1.0.0" ) );

    RequireJsDependencyResolver resolver = new RequireJsDependencyResolver( packages );

    IRequireJsPackageConfiguration resolvedPackage = resolver.getResolvedVersion( "A", "1.0.0" );
    assertEquals( "Returns the requested version", this.mockPackageA_1_0_0.getVersion(), resolvedPackage.getVersion() );
  }

  @Test
  public void getResolvedVersionBiggerThan() {
    packages.add( createMockPackage( "B", "1.0.0", "A", ">1.0" ) );

    RequireJsDependencyResolver resolver = new RequireJsDependencyResolver( packages );

    IRequireJsPackageConfiguration resolvedPackage = resolver.getResolvedVersion( "A", ">1.0" );
    assertEquals( "Returns the higher version", this.mockPackageA_2_5_0.getVersion(), resolvedPackage.getVersion() );
  }

  @Test
  public void getResolvedVersionSmallerThan() {
    packages.add( createMockPackage( "B", "1.0.0", "A", "<1.8" ) );

    RequireJsDependencyResolver resolver = new RequireJsDependencyResolver( packages );

    IRequireJsPackageConfiguration resolvedPackage = resolver.getResolvedVersion( "A", "<1.8" );
    assertEquals( "Returns the higher respecting the condition", this.mockPackageA_1_5_0.getVersion(), resolvedPackage.getVersion() );
  }

  @Test
  public void getResolvedVersionBiggerThanConditioned() {
    packages.add( createMockPackage( "B", "1.0.0", "A", ">1.0" ) );
    packages.add( createMockPackage( "C", "1.0.0", "A", "<1.7" ) );
    packages.add( createMockPackage( "D", "1.0.0", "A", ">2.0" ) );

    RequireJsDependencyResolver resolver = new RequireJsDependencyResolver( packages );

    IRequireJsPackageConfiguration resolvedPackage = resolver.getResolvedVersion( "A", ">1.0" );
    assertEquals( this.mockPackageA_1_5_0.getVersion(), resolvedPackage.getVersion() );

    resolvedPackage = resolver.getResolvedVersion( "A", "<1.7" );
    assertEquals( this.mockPackageA_1_5_0.getVersion(), resolvedPackage.getVersion() );

    resolvedPackage = resolver.getResolvedVersion( "A", ">2.0" );
    assertEquals( this.mockPackageA_2_5_0.getVersion(), resolvedPackage.getVersion() );
  }

  @Test
  public void getResolvedVersionTheExactRulesIt() {
    packages.add( createMockPackage( "B", "1.0.0", "A", ">1.0" ) );
    packages.add( createMockPackage( "C", "1.0.0", "A", "1.5" ) );
    packages.add( createMockPackage( "D", "1.0.0", "A", "<2.0" ) );

    // the majority (all) will be ok with 1.5.0

    RequireJsDependencyResolver resolver = new RequireJsDependencyResolver( packages );

    IRequireJsPackageConfiguration resolvedPackage = resolver.getResolvedVersion( "A", ">1.0" );
    assertEquals( this.mockPackageA_1_5_0.getVersion(), resolvedPackage.getVersion() );

    resolvedPackage = resolver.getResolvedVersion( "A", "1.5" );
    assertEquals( this.mockPackageA_1_5_0.getVersion(), resolvedPackage.getVersion() );

    resolvedPackage = resolver.getResolvedVersion( "A", "<2.0" );
    assertEquals( this.mockPackageA_1_5_0.getVersion(), resolvedPackage.getVersion() );
  }

  @Test
  public void getResolvedVersionTheMajoritySetsTheMax() {
    packages.add( createMockPackage( "B", "1.0.0", "A", ">1.0" ) );
    packages.add( createMockPackage( "C", "1.0.0", "A", "1.5" ) );
    packages.add( createMockPackage( "D", "1.0.0", "A", "<2.0" ) );
    packages.add( createMockPackage( "E", "1.0.0", "A", ">1.7" ) );

    // the majority (B, D, E) will be ok with 1.8.0, and C will get 1.5.0

    RequireJsDependencyResolver resolver = new RequireJsDependencyResolver( packages );

    IRequireJsPackageConfiguration resolvedPackage = resolver.getResolvedVersion( "A", ">1.0" );
    assertEquals( this.mockPackageA_1_8_0.getVersion(), resolvedPackage.getVersion() );

    resolvedPackage = resolver.getResolvedVersion( "A", "1.5" );
    assertEquals( this.mockPackageA_1_5_0.getVersion(), resolvedPackage.getVersion() );

    resolvedPackage = resolver.getResolvedVersion( "A", "<2.0" );
    assertEquals( this.mockPackageA_1_8_0.getVersion(), resolvedPackage.getVersion() );

    resolvedPackage = resolver.getResolvedVersion( "A", ">1.7" );
    assertEquals( this.mockPackageA_1_8_0.getVersion(), resolvedPackage.getVersion() );
  }

  @Test
  public void getResolvedVersionOtherMajority() {
    packages.add( createMockPackage( "B", "1.0.0", "A", "<1.7" ) );
    packages.add( createMockPackage( "C", "1.0.0", "A", "<1.4" ) );
    packages.add( createMockPackage( "D", "1.0.0", "A", ">1.0" ) );
    packages.add( createMockPackage( "E", "1.0.0", "A", ">1.9" ) );
    packages.add( createMockPackage( "F", "1.0.0", "A", ">1.4 & <1.6" ) );
    packages.add( createMockPackage( "G", "1.0.0", "A", ">2.0" ) );
    packages.add( createMockPackage( "H", "1.0.0", "A", ">2.1" ) );

    // the majority (D, E, G, H) will be ok with 2.5.0, and B and F will get 1.5.0, while C gets 1.3.0

    RequireJsDependencyResolver resolver = new RequireJsDependencyResolver( packages );

    IRequireJsPackageConfiguration resolvedPackage = resolver.getResolvedVersion( "A", "<1.7" );
    assertEquals( this.mockPackageA_1_5_0.getVersion(), resolvedPackage.getVersion() );

    resolvedPackage = resolver.getResolvedVersion( "A", "<1.4" );
    assertEquals( this.mockPackageA_1_3_0.getVersion(), resolvedPackage.getVersion() );

    resolvedPackage = resolver.getResolvedVersion( "A", ">1.0" );
    assertEquals( this.mockPackageA_2_5_0.getVersion(), resolvedPackage.getVersion() );

    resolvedPackage = resolver.getResolvedVersion( "A", ">1.9" );
    assertEquals( this.mockPackageA_2_5_0.getVersion(), resolvedPackage.getVersion() );

    resolvedPackage = resolver.getResolvedVersion( "A", ">1.4 & <1.6" );
    assertEquals( this.mockPackageA_1_5_0.getVersion(), resolvedPackage.getVersion() );

    resolvedPackage = resolver.getResolvedVersion( "A", ">2.0" );
    assertEquals( this.mockPackageA_2_5_0.getVersion(), resolvedPackage.getVersion() );

    resolvedPackage = resolver.getResolvedVersion( "A", ">2.1" );
    assertEquals( this.mockPackageA_2_5_0.getVersion(), resolvedPackage.getVersion() );
  }

  @Test
  public void getResolvedVersionTheMajorityChoosesLowerTestA() {
    // this is the case to be solved later by the TODO
    // notice a majority of packages will be better served with 2.5.0
    // however that number isn't taken into account at the moment
    // only the number of conditions satisfied
    // this is absurd... simply changing the package F condition to >1.9.1
    // would change the end result (see getResolvedVersionTheMajorityChoosesLowerTestB)

    packages.add( createMockPackage( "B", "1.0.0", "A", "<1.7" ) );
    packages.add( createMockPackage( "C", "1.0.0", "A", "<1.6" ) );
    packages.add( createMockPackage( "D", "1.0.0", "A", ">1.7" ) );
    packages.add( createMockPackage( "E", "1.0.0", "A", ">1.9" ) );
    packages.add( createMockPackage( "E", "1.0.0", "A", ">1.9" ) );
    packages.add( createMockPackage( "F", "1.0.0", "A", ">1.9" ) );
    packages.add( createMockPackage( "G", "1.0.0", "A", ">1.0" ) );

    RequireJsDependencyResolver resolver = new RequireJsDependencyResolver( packages );

    IRequireJsPackageConfiguration resolvedPackage = resolver.getResolvedVersion( "A", ">1.0" );
    assertEquals( this.mockPackageA_1_5_0.getVersion(), resolvedPackage.getVersion() );
  }

  @Test
  public void getResolvedVersionTheMajorityChoosesLowerTestB() {
    packages.add( createMockPackage( "B", "1.0.0", "A", "<1.7" ) );
    packages.add( createMockPackage( "C", "1.0.0", "A", "<1.6" ) );
    packages.add( createMockPackage( "D", "1.0.0", "A", ">1.7" ) );
    packages.add( createMockPackage( "E", "1.0.0", "A", ">1.9" ) );
    packages.add( createMockPackage( "E", "1.0.0", "A", ">1.9" ) );
    packages.add( createMockPackage( "F", "1.0.0", "A", ">1.9.1" ) );
    packages.add( createMockPackage( "G", "1.0.0", "A", ">1.0" ) );

    RequireJsDependencyResolver resolver = new RequireJsDependencyResolver( packages );

    IRequireJsPackageConfiguration resolvedPackage = resolver.getResolvedVersion( "A", ">1.0" );
    assertEquals( this.mockPackageA_2_5_0.getVersion(), resolvedPackage.getVersion() );
  }

  @Test
  public void getResolvedVersionNotInstalledDependency() {
    packages.add( createMockPackage( "B", "1.0.0", "Unknown", "<1.8" ) );

    RequireJsDependencyResolver resolver = new RequireJsDependencyResolver( packages );

    IRequireJsPackageConfiguration resolvedPackage = resolver.getResolvedVersion( "A", "<1.8" );
    assertNull( "Returns null", resolvedPackage );
  }

  @Test
  public void getResolvedVersionExactNonSemanticVersion() {
    packages.add( createMockPackage( "B", "1.0-SNAPSHOT" ) );
    packages.add( createMockPackage( "C", "1.0.0", "B", "1.0-SNAPSHOT" ) );

    RequireJsDependencyResolver resolver = new RequireJsDependencyResolver( packages );

    IRequireJsPackageConfiguration resolvedPackage = resolver.getResolvedVersion( "B", "1.0-SNAPSHOT" );
    assertEquals( "Returns the requested version", "1.0-SNAPSHOT", resolvedPackage.getVersion() );
  }

  @Test
  public void getResolvedVersionNonExactNonSemanticVersion() {
    packages.add( createMockPackage( "B", "1.5-SNAPSHOT" ) );
    packages.add( createMockPackage( "C", "1.0.0", "B", ">1.0.0" ) );

    RequireJsDependencyResolver resolver = new RequireJsDependencyResolver( packages );

    IRequireJsPackageConfiguration resolvedPackage = resolver.getResolvedVersion( "B", ">1.0-SNAPSHOT" );
    assertNull( "Returns null", resolvedPackage );
  }

  @Test
  public void getResolvedVersionNonExactNonSemanticVersionWithOthers() {
    packages.add( createMockPackage( "A", "1.6-SNAPSHOT" ) );
    packages.add( createMockPackage( "B", "1.0.0", "A", "<1.7" ) );

    RequireJsDependencyResolver resolver = new RequireJsDependencyResolver( packages );

    IRequireJsPackageConfiguration resolvedPackage = resolver.getResolvedVersion( "A", "<1.7" );
    assertEquals( "Returns the higher respecting the condition", this.mockPackageA_1_5_0.getVersion(), resolvedPackage.getVersion() );
  }

  @Test
  public void getResolvedVersionRelaxOnMinorVersion() {
    packages.add( createMockPackage( "B", "1.0.2" ) );
    packages.add( createMockPackage( "B", "1.0.4" ) );
    packages.add( createMockPackage( "C", "1.0.0", "B", "1.0.0" ) );

    RequireJsDependencyResolver resolver = new RequireJsDependencyResolver( packages );

    IRequireJsPackageConfiguration resolvedPackage = resolver.getResolvedVersion( "B", "1.0.0" );
    assertEquals( "Returns the higher minor version", "1.0.4", resolvedPackage.getVersion() );
  }

  @Test
  public void getResolvedVersionNotFound() {
    packages.add( createMockPackage( "B", "1.0.0", "A", ">3.0" ) );
    packages.add( createMockPackage( "C", "1.0.0", "A", "3.0" ) );

    RequireJsDependencyResolver resolver = new RequireJsDependencyResolver( packages );

    IRequireJsPackageConfiguration resolvedPackage = resolver.getResolvedVersion( "A", ">3.0" );
    assertNull( "Returns null", resolvedPackage );

    resolvedPackage = resolver.getResolvedVersion( "A", "3.0" );
    assertNull( "Returns null", resolvedPackage );
  }
}