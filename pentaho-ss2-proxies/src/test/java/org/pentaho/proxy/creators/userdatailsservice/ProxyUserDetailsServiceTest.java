package org.pentaho.proxy.creators.userdatailsservice;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.proxy.creators.userdetailsservice.ProxyUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.util.ReflectionUtils;

public class ProxyUserDetailsServiceTest {
  private Logger logger = LoggerFactory.getLogger( getClass() );

  UserDetails mockUserDetails;
  UserDetailsService mockUserDetailsService;

  private static final String MOCK_USERNAME = "MOCK_USERNAME";
  private static final String MOCK_PASSWORD = "MOCK_PASSWORD";
  private static final String MOCK_USERNAME_NOT_EXISTS = "MOCK_USERNAME_NOT_EXISTS";
  private static final String MOCK_USERNAME_NOT_EXISTS_RETURNS_NULL = "MOCK_USERNAME_NOT_EXISTS_RETURNS_NULL";
  private static final boolean MOCK_IS_ENABLED = true;

  @Before
  public void setUp() throws NoSuchMethodException {

    mockUserDetails = mock( UserDetails.class );
    mockUserDetailsService = mock( UserDetailsService.class );

    when( mockUserDetails.getUsername() ).thenReturn( MOCK_USERNAME );
    when( mockUserDetails.getPassword() ).thenReturn( MOCK_PASSWORD );
    when( mockUserDetails.isEnabled() ).thenReturn( MOCK_IS_ENABLED );

    when( mockUserDetailsService.loadUserByUsername( MOCK_USERNAME ) ).thenReturn( mockUserDetails );
    when( mockUserDetailsService.loadUserByUsername( MOCK_USERNAME_NOT_EXISTS ) ).thenThrow( new UsernameNotFoundException( MOCK_USERNAME_NOT_EXISTS ) );
    when( mockUserDetailsService.loadUserByUsername( MOCK_USERNAME_NOT_EXISTS_RETURNS_NULL ) ).thenReturn( null );
  }

  @Test public void testCreateProxyWrapper() {

    Object wrappedObject = new ProxyUserDetailsService( mockUserDetailsService );

    Assert.assertNotNull( wrappedObject );

    Method loadUserByUsernameMethod =
        ReflectionUtils.findMethod( wrappedObject.getClass(), "loadUserByUsername", String.class  );

    try {

      Object wrappedUserDetails = loadUserByUsernameMethod.invoke( wrappedObject, MOCK_USERNAME );

      Assert.assertNotNull( wrappedUserDetails );

      Method getUsernameMethod = ReflectionUtils.findMethod( wrappedUserDetails.getClass(), "getUsername" );
      Method getPasswordMethod = ReflectionUtils.findMethod( wrappedUserDetails.getClass(), "getPassword" );
      Method isEnabledMethod = ReflectionUtils.findMethod( wrappedUserDetails.getClass(), "isEnabled" );

      Object wrappedUsername = getUsernameMethod.invoke( wrappedUserDetails );
      Object wrappedPassword = getPasswordMethod.invoke( wrappedUserDetails );
      Object wrappedIsEnabled = isEnabledMethod.invoke( wrappedUserDetails );

      Assert.assertNotNull( wrappedUsername );
      Assert.assertNotNull( wrappedPassword );
      Assert.assertNotNull( wrappedIsEnabled );

      Assert.assertTrue( wrappedUsername.toString().equals( MOCK_USERNAME ) );
      Assert.assertTrue( wrappedPassword.toString().equals( MOCK_PASSWORD ) );
      Assert.assertTrue( wrappedIsEnabled instanceof Boolean && ( wrappedIsEnabled ).equals( MOCK_IS_ENABLED ) );

    } catch ( IllegalAccessException | InvocationTargetException e ) {
      logger.error( e.getMessage(), e );
      Assert.fail();
    }
  }

  @Test public void testNoUserDetailsProxyWrapper() {

    Object wrappedObject = new ProxyUserDetailsService( mockUserDetailsService );

    Assert.assertNotNull( wrappedObject );

    Method loadUserByUsernameMethod =
        ReflectionUtils.findMethod( wrappedObject.getClass(), "loadUserByUsername", String.class );

    try {
      loadUserByUsernameMethod.invoke( wrappedObject, MOCK_USERNAME_NOT_EXISTS );
      Assert.fail();

    } catch ( InvocationTargetException e ) {
      Assert.assertTrue( e.getCause() instanceof UsernameNotFoundException );
    } catch ( Throwable t ) {
      logger.error( t.getMessage(), t );
      Assert.fail();
    }

    try {
      loadUserByUsernameMethod.invoke( wrappedObject, MOCK_USERNAME_NOT_EXISTS_RETURNS_NULL );
      Assert.fail();

    } catch ( InvocationTargetException e ) {
      Assert.assertTrue( e.getCause() instanceof UsernameNotFoundException );
    } catch ( Throwable t ) {
      logger.error( t.getMessage(), t );
      Assert.fail();
    }
  }

  @After
  public void tearDown() {
    mockUserDetails = null;
    mockUserDetailsService = null;
  }
}
