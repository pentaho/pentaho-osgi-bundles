package org.pentaho.proxy.creators.securitycontext;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.api.IProxyRegistration;
import org.pentaho.platform.proxy.impl.ProxyException;
import org.pentaho.proxy.creators.authenticationentrypoint.AuthenticationExceptionProxyCreator;

import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.context.SecurityContext;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class AuthenticationExceptionProxyCreatorTest {

  private static final String MOCK_PRINCIPAL = "MOCK_PRINCIPAL";
  private static final boolean MOCK_PRINCIPAL_IS_AUTHENTICATED = true;

  Authentication mockAuthentication;
  AuthenticationException mockAuthenticationException;

  @Before
  public void setUp() throws NoSuchMethodException {

    mockAuthentication = mock( Authentication.class );

    when( mockAuthentication.getPrincipal() ).thenReturn( MOCK_PRINCIPAL );
    when( mockAuthentication.isAuthenticated() ).thenReturn( MOCK_PRINCIPAL_IS_AUTHENTICATED );

    mockAuthenticationException = mock( AuthenticationException.class );
    when( mockAuthenticationException.getAuthentication() ).thenReturn( mockAuthentication );

  }

  @Test public void testCreateProxyWrapper() {

    AuthenticationException wrappedObject = new AuthenticationExceptionProxyCreatorForTest().create( mockAuthenticationException );

    Assert.assertNotNull( wrappedObject );
    Assert.assertNotNull( wrappedObject.getAuthentication() );
    Assert.assertTrue( wrappedObject.getAuthentication().getPrincipal().equals( MOCK_PRINCIPAL ) );
    Assert.assertTrue( wrappedObject.getAuthentication().isAuthenticated() == MOCK_PRINCIPAL_IS_AUTHENTICATED );
  }

  @After
  public void tearDown(){
    mockAuthentication = null;
    mockAuthenticationException = null;
  }

  private class AuthenticationExceptionProxyCreatorForTest extends AuthenticationExceptionProxyCreator {

    @Override public boolean supports( Class aClass ) {
      return true;
    }

    @Override public IProxyFactory getProxyFactory() {
      return new IProxyFactory(){

        @Override public <T, K> IProxyRegistration createAndRegisterProxy( T target, List<Class<?>> publishedClasses,
            Map<String, Object> properties ) throws ProxyException {
          return null;
        }

        @Override public <T, K> K createProxy( T target ) throws ProxyException {
          return ( K ) target;
        }
      };
    }
  }
}
