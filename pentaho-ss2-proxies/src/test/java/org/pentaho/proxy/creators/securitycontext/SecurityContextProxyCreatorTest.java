package org.pentaho.proxy.creators.securitycontext;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.api.IProxyRegistration;
import org.pentaho.platform.proxy.impl.ProxyException;
import org.pentaho.proxy.creators.securitycontext.SecurityContextProxyCreator;

import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContext;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class SecurityContextProxyCreatorTest {

  private static final String MOCK_PRINCIPAL = "MOCK_PRINCIPAL";
  private static final boolean MOCK_PRINCIPAL_IS_AUTHENTICATED = true;

  Authentication mockAuthentication;
  SecurityContext mockSecurityContext;

  @Before
  public void setUp() throws NoSuchMethodException {

    mockAuthentication = mock( Authentication.class );

    when( mockAuthentication.getPrincipal() ).thenReturn( MOCK_PRINCIPAL );
    when( mockAuthentication.isAuthenticated() ).thenReturn( MOCK_PRINCIPAL_IS_AUTHENTICATED );

    mockSecurityContext = mock( SecurityContext.class );
    when( mockSecurityContext.getAuthentication() ).thenReturn( mockAuthentication );

  }

  @Test public void testCreateProxyWrapper() {

    SecurityContext wrappedObject = new SecurityContextProxyCreatorForTest().create( mockSecurityContext );

    Assert.assertNotNull( wrappedObject );
    Assert.assertNotNull( wrappedObject.getAuthentication() );
    Assert.assertTrue( wrappedObject.getAuthentication().getPrincipal().equals( MOCK_PRINCIPAL ) );
    Assert.assertTrue( wrappedObject.getAuthentication().isAuthenticated() == MOCK_PRINCIPAL_IS_AUTHENTICATED );
  }

  @After
  public void tearDown(){
    mockAuthentication = null;
    mockSecurityContext = null;
  }

  private class SecurityContextProxyCreatorForTest extends SecurityContextProxyCreator {

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
