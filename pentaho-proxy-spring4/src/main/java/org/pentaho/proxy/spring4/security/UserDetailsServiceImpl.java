package org.pentaho.proxy.spring4.security;

import java.util.HashMap;
import java.util.Map;

import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.memory.UserAttribute;
import org.springframework.security.core.userdetails.memory.UserAttributeEditor;

/**
 * Created by tkafalas on 8/24/15.
 */
public class UserDetailsServiceImpl implements UserDetailsService {
  HashMap<String, UserDetailsImpl> userDetailsList = new HashMap<String, UserDetailsImpl>();
  ITenantedPrincipleNameResolver tenantedPrincipleNameResolver;

  public UserDetailsServiceImpl( ITenantedPrincipleNameResolver tenantedPrincipleNameResolver,
      Map<String, String> userDefMap ) {
    this.tenantedPrincipleNameResolver = tenantedPrincipleNameResolver;
    ITenant tenant = new Tenant( "/pentaho/tenant1", true );
    for ( String username : userDefMap.keySet() ) {
      String principleId = tenantedPrincipleNameResolver.getPrincipleId( tenant, username );
      UserAttributeEditor userAttributeEditor = new UserAttributeEditor();
      userAttributeEditor.setAsText( userDefMap.get( username ) );
      userDetailsList.put( principleId, new UserDetailsImpl( principleId, (UserAttribute) userAttributeEditor
          .getValue() ) );
    }
  }

  @Override
  public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException {
    String principalId = tenantedPrincipleNameResolver.getPrincipleId( JcrTenantUtils.getCurrentTenant(),
        username );
    if ( userDetailsList.get( principalId ) == null ) {
      throw new UsernameNotFoundException( "user not found" );
    }
    return userDetailsList.get( principalId );
  }

}
