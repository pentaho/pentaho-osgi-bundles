/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.osgi.auth.spring;

import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.apache.karaf.jaas.modules.AbstractKarafLoginModule;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * JAAS LoginModule which delegates to the Platform's Spring Security
 * {@link org.springframework.security.authentication.AuthenticationManager}.
 *
 * If the Authenticated user has AdministerSecurity permissions, they'll be given a synthetic role of "karaf_admin"
 * which provides access to Admin features of Karaf
 *
 * Created by nbaker on 8/26/14.
 */
public class SpringSecurityLoginModule extends AbstractKarafLoginModule {

  private AuthenticationManager authenticationManager = null;
  private IAuthorizationPolicy  authorizationPolicy   = null;

  public SpringSecurityLoginModule() {

  }

  public AuthenticationManager getAuthenticationManager() {
    if ( authenticationManager == null ) {
      authenticationManager = PentahoSystem.get( AuthenticationManager.class );
    }
    return authenticationManager;
  }

  public IAuthorizationPolicy getAuthorizationPolicy() {
    if ( authorizationPolicy == null ) {
      authorizationPolicy = PentahoSystem.get( IAuthorizationPolicy.class );
    }
    return authorizationPolicy;
  }

  public void setAuthenticationManager( AuthenticationManager authenticationManager ) {
    this.authenticationManager = authenticationManager;
  }

  public void setAuthorizationPolicy( IAuthorizationPolicy authorizationPolicy ) {
    this.authorizationPolicy = authorizationPolicy;
  }

  public void initialize( Subject sub, CallbackHandler handler, Map sharedState, Map options ) {
    super.initialize( sub, handler, options );
  }

  public boolean login() throws LoginException {

    org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if ( authentication != null ) {
      // Obtain the username of the incoming auth request to match against existing authentication on the thread.

      Callback[] callbacks = new Callback[1];
      callbacks[0] = new NameCallback( "User: " );

      try {
        callbackHandler.handle( callbacks );
      } catch ( IOException e ) {
        throw new LoginException( e.getMessage() );
      } catch ( UnsupportedCallbackException e ) {
        throw new LoginException( "Unable to interactively Authenticate with user: " + e.getMessage() );
      }
      // user callback get value
      String name = ( (NameCallback) callbacks[0] ).getName();
      if ( name == null ) {
        throw new LoginException( "User name is null" );
      }

      // If the existing thread-bound authentication does not match, discard it.
      if ( !name.equals( authentication.getName() ) ) {
        // reauthenticate
        authentication = null;
      }

    }

    if ( authentication == null ) {

      Callback[] callbacks = new Callback[2];

      callbacks[0] = new NameCallback( "User: " );
      callbacks[1] = new PasswordCallback( "Password: ", false );
      try {
        callbackHandler.handle( callbacks );
      } catch ( IOException e ) {
        throw new LoginException( e.getMessage() );
      } catch ( UnsupportedCallbackException e ) {
        throw new LoginException( "Unable to interactively Authenticate with user: " + e.getMessage() );
      }

      String name = ( (NameCallback) callbacks[0] ).getName();
      char[] password1 = ( (PasswordCallback) callbacks[1] ).getPassword();

      if ( password1 == null || name == null ) {
        throw new LoginException( "User Name and Password cannot be null" );
      }
      String password = new String( password1 );

      UsernamePasswordAuthenticationToken token =
          new UsernamePasswordAuthenticationToken( name, String.valueOf( password ) );

      IPentahoSession session = new StandaloneSession( name );
      PentahoSessionHolder.setSession( session );
      try {
        // Throws an exception on failure.
        authentication = getAuthenticationManager().authenticate( token );
        if ( authentication != null && !authentication.isAuthenticated() ) {
          throw new IllegalStateException( "Got a bad authentication" );
        }
        if ( authentication == null ) {
          throw new IllegalStateException( "Not Authenticated" );
        }
      } catch ( Exception e ) {
        session.destroy();
        PentahoSessionHolder.removeSession();
        throw new LoginException( e.getMessage() );
      }
    }

    principals = new HashSet<Principal>();
    principals.add( new UserPrincipal( authentication.getName() ) );
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    if ( authorities != null ) {
      for ( GrantedAuthority authority : authorities ) {
        principals.add( new RolePrincipal( authority.getAuthority() ) );
      }
    }

    // AuthorizationPolicy requires a PentahoSession. becomeUSer is the easiest way
    SecurityHelper.getInstance().becomeUser( authentication.getName() );

    // If they have AdministerSecurity, grant the Karaf admin role
    if ( getAuthorizationPolicy().isAllowed( AdministerSecurityAction.NAME ) ) {
      principals.addAll( getKarafAdminPrincipals() );
    }

    succeeded = true;

    return true;
  }

  /**
   * Retrieve standard installation karaf administrative roles.
   * List of roles copied from user 'karaf' in file
   * <a href="https://github.com/pentaho/karaf/blob/master/assemblies/features/base/src/main/resources/resources/etc/users.properties">user.properties</a>
   * @return set of administrative principals
   */
  protected Set<Principal> getKarafAdminPrincipals() {
    HashSet<Principal> adminPrincipals = new HashSet<Principal>();
    // copying "etc/users.properties" roles for default installation karaf user
    String roleNames = "group,admin,manager,viewer,systembundles,ssh";
    for ( String roleName : roleNames.split( "," )  ) {
      adminPrincipals.add( new RolePrincipal( roleName ) );
    }
    return adminPrincipals;
  }

  public boolean abort() throws LoginException {
    clear();
    return true;
  }

  public boolean logout() throws LoginException {
    subject.getPrincipals().removeAll( principals );
    principals.clear();
    return true;
  }

}
