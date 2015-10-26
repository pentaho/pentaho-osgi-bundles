package org.pentaho.proxy.creators.grantedauthorities;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.api.IProxyRegistration;
import org.pentaho.platform.proxy.impl.ProxyException;

import org.pentaho.proxy.creators.grantedauthorities.S4GrantedAuthorityProxyCreator;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Map;

public class S4GrantedAuthorityProxyCreatorTest {

  private static final String MOCK_ROLE_AUTHENTICATED = "MockRoleAuthenticated";

  private GrantedAuthority mockGrantedAuthority;

  @Before
  public void setUp() throws NoSuchMethodException {

    mockGrantedAuthority = new GrantedAuthority() {
      @Override public String getAuthority() {
        return MOCK_ROLE_AUTHENTICATED;
      }
    };
  }

  @Test public void testCreateProxyWrapper() {

    GrantedAuthority wrappedObject = new S4GrantedAuthoritiesProxyCreatorForTest().create( mockGrantedAuthority );

    Assert.assertNotNull( wrappedObject );
    Assert.assertTrue( MOCK_ROLE_AUTHENTICATED.equals( wrappedObject.getAuthority() ) );
  }

  @After
  public void tearDown() {
    mockGrantedAuthority = null;
  }

  private class S4GrantedAuthoritiesProxyCreatorForTest extends S4GrantedAuthorityProxyCreator {

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
