package org.pentaho.proxy.creators.userdetailsservice;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class S4UserDetailsProxyCreatorTest {

  private Logger logger = LoggerFactory.getLogger( getClass() );

  UserDetails mockUserDetails;

  private static final String MOCK_USERNAME = "MOCK_USERNAME";
  private static final String MOCK_PASSWORD = "MOCK_PASSWORD";
  private static final boolean MOCK_IS_ENABLED = true;
  private static final boolean MOCK_IS_CREDENTIALS_NON_EXPIRED = true;
  private static final boolean MOCK_IS_ACCOUNT_NON_EXPIRED = true;
  private static final boolean MOCK_IS_ACCOUNT_NON_LOCKED = true;

  @Before
  public void setUp() throws NoSuchMethodException {

    mockUserDetails = mock( UserDetails.class );

    when( mockUserDetails.getUsername() ).thenReturn( MOCK_USERNAME );
    when( mockUserDetails.getPassword() ).thenReturn( MOCK_PASSWORD );
    when( mockUserDetails.isEnabled() ).thenReturn( MOCK_IS_ENABLED );
    when( mockUserDetails.isAccountNonExpired() ).thenReturn( MOCK_IS_ACCOUNT_NON_EXPIRED );
    when( mockUserDetails.isAccountNonLocked() ).thenReturn( MOCK_IS_ACCOUNT_NON_LOCKED );
    when( mockUserDetails.isCredentialsNonExpired() ).thenReturn( MOCK_IS_CREDENTIALS_NON_EXPIRED );
  }

  @Test public void testCreateProxyWrapper() {

    Object wrappedObject = new S4UserDetailsProxyCreatorForTest().create( mockUserDetails );

    Assert.assertNotNull( wrappedObject );

    Method getUsernameMethod = ReflectionUtils.findMethod( wrappedObject.getClass(), "getUsername" );
    Method getPasswordMethod = ReflectionUtils.findMethod( wrappedObject.getClass(), "getPassword" );
    Method isEnabledMethod = ReflectionUtils.findMethod( wrappedObject.getClass(), "isEnabled" );
    Method isAccountNonExpiredMethod = ReflectionUtils.findMethod( wrappedObject.getClass(), "isAccountNonExpired" );
    Method isAccountNonLockedMethod = ReflectionUtils.findMethod( wrappedObject.getClass(), "isAccountNonLocked" );
    Method isCredentialsNonExpiredMethod = ReflectionUtils.findMethod( wrappedObject.getClass(), "isCredentialsNonExpired" );

    try {

      Object wrappedUsername = getUsernameMethod.invoke( wrappedObject );
      Object wrappedPassword = getPasswordMethod.invoke( wrappedObject );
      Object wrappedIsEnabled = isEnabledMethod.invoke( wrappedObject );
      Object wrappedIsAccountNonExpired = isAccountNonExpiredMethod.invoke( wrappedObject );
      Object wrappedIsAccountNonLocked = isAccountNonLockedMethod.invoke( wrappedObject );
      Object wrappedIsCredentialsNonExpired = isCredentialsNonExpiredMethod.invoke( wrappedObject );

      Assert.assertNotNull( wrappedUsername );
      Assert.assertNotNull( wrappedPassword );
      Assert.assertNotNull( wrappedIsEnabled );
      Assert.assertNotNull( wrappedIsAccountNonExpired );
      Assert.assertNotNull( wrappedIsAccountNonLocked );
      Assert.assertNotNull( wrappedIsCredentialsNonExpired );

      Assert.assertTrue( wrappedUsername.toString().equals( MOCK_USERNAME ) );
      Assert.assertTrue( wrappedPassword.toString().equals( MOCK_PASSWORD ) );
      Assert.assertTrue( wrappedIsEnabled instanceof Boolean && ( wrappedIsEnabled ).equals( MOCK_IS_ENABLED ) );
      Assert.assertTrue( wrappedIsAccountNonExpired instanceof Boolean
          && ( wrappedIsAccountNonExpired ).equals( MOCK_IS_ACCOUNT_NON_EXPIRED ) );
      Assert.assertTrue( wrappedIsAccountNonLocked instanceof Boolean
          && ( wrappedIsAccountNonLocked ).equals( MOCK_IS_ACCOUNT_NON_LOCKED ) );
      Assert.assertTrue( wrappedIsCredentialsNonExpired instanceof Boolean
          && ( wrappedIsCredentialsNonExpired ).equals( MOCK_IS_CREDENTIALS_NON_EXPIRED ) );

    } catch ( IllegalAccessException | InvocationTargetException e ) {
      logger.error( e.getMessage(), e );
      Assert.fail();
    }
  }

  @After
  public void tearDown() {
    mockUserDetails = null;
  }

  private class S4UserDetailsProxyCreatorForTest extends S4UserDetailsProxyCreator {

    @Override public boolean supports( Class aClass ) {
      return true;
    }
  }
}
