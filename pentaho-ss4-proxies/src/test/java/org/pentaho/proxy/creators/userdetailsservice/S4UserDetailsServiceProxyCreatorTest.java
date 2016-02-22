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
 * Copyright 2016 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.proxy.creators.userdetailsservice;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.api.IProxyRegistration;
import org.pentaho.platform.proxy.impl.ProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class S4UserDetailsServiceProxyCreatorTest {

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

    Object wrappedObject = new S4UserDetailsServiceProxyCreatorForTest().create( mockUserDetailsService );

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

    Object wrappedObject = new S4UserDetailsServiceProxyCreatorForTest().create( mockUserDetailsService );

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


  private class S4UserDetailsServiceProxyCreatorForTest extends S4UserDetailsServiceProxyCreator {

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
