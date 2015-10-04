package org.pentaho.proxy.creators.authenticationmanager;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.api.IProxyRegistration;
import org.pentaho.platform.proxy.impl.ProxyException;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.AuthenticationManager;

import org.springframework.security.context.SecurityContext;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;


public class AuthenticationManagerProxyCreatorTest {

  private static final String MOCK_PRINCIPAL = "MOCK_PRINCIPAL";
  private static final String MOCK_NAME = "MOCK_NAME";

  Authentication mockAuthentication;
  AuthenticationManager mockAuthenticationManager;

  @Before
  public void setUp() throws NoSuchMethodException {

    mockAuthentication = mock( Authentication.class );

    when( mockAuthentication.getPrincipal() ).thenReturn( MOCK_PRINCIPAL );
    when( mockAuthentication.getName() ).thenReturn( MOCK_NAME );

    mockAuthenticationManager = mock( AuthenticationManager.class );
    when( mockAuthenticationManager.authenticate( any( Authentication.class ) ) ).thenReturn( mockAuthentication );
  }

  @Test public void testCreateProxyWrapper() {

    AuthenticationManager wrappedObject = new AuthenticationManagerProxyCreatorForTest().create( mockAuthenticationManager );

    Assert.assertNotNull( wrappedObject );

    Authentication returnAuth = null;
    try {
      Assert.assertNotNull( ( returnAuth = wrappedObject.authenticate( mockAuthentication ) ) );
    } catch ( Exception e ) {
      Assert.fail();
    }

    Assert.assertTrue( returnAuth.getPrincipal().equals( MOCK_PRINCIPAL ) );
    Assert.assertTrue( returnAuth.getName().equals( MOCK_NAME ) );
  }

  @After
  public void tearDown(){
    mockAuthenticationManager = null;
    mockAuthentication = null;
  }

  private class AuthenticationManagerProxyCreatorForTest extends AuthenticationManagerProxyCreator{

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
