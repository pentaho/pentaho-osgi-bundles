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
package org.pentaho.platform.pdi;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Created by nbaker on 10/1/16.
 */
public class AgileBiAuthorizationPolicyTest {

  private AgileBiAuthorizationPolicy authorizationPolicy = new AgileBiAuthorizationPolicy();

  @Test
  public void isAllowed() throws Exception {
    assertThat( authorizationPolicy.isAllowed( "anything" ), is( true ) );
  }

  @Test
  public void getAllowedActions() throws Exception {
    assertThat( authorizationPolicy.getAllowedActions( "anything" ), equalTo( Arrays
      .asList( "org.pentaho.repository.read", "org.pentaho.repository.create",
        "org.pentaho.security.administerSecurity" )));
  }

}