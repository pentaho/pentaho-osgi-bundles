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