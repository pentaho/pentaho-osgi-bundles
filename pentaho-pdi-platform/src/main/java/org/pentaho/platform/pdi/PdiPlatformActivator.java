package org.pentaho.platform.pdi;

import mondrian.olap.MondrianProperties;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.pentaho.platform.api.engine.IAclVoter;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.pdi.vfs.MetadataToMondrianVfs;
import org.pentaho.platform.plugin.services.pluginmgr.PentahoSystemPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by nbaker on 8/31/16.
 */
public class PdiPlatformActivator implements BundleActivator {
  public static final IAclVoter ACL_VOTER = new AgileBiAclVoter();
  public static final IPluginResourceLoader RESOURCE_LOADER = new AgileBiPluginResourceLoader();

  private Logger logger = LoggerFactory.getLogger( getClass() );

  @Override public void start( BundleContext bundleContext ) throws Exception {

    // There is no AclVoter when running within the PDI client. We add our own.
    if ( PentahoSystem.get( IAclVoter.class ) == null ) {
      PentahoSystem.registerObject( ACL_VOTER );
    }


    if ( PentahoSystem.get( IPluginResourceLoader.class ) == null ) {
      PentahoSystem.registerObject( RESOURCE_LOADER );
    }

    if ( PentahoSystem.get( IPluginManager.class ) == null ) {
      PentahoSystem.registerObject( new PentahoSystemPluginManager() {
        @Override public Object getPluginSetting( String pluginId, String key, String defaultValue ) {
          return null;
        }
      } );
    }

    // We're the one who boots PentahoSystem when running outside of the server
    if ( PentahoSystem.getInitializedStatus() != PentahoSystem.SYSTEM_INITIALIZED_OK ) {
      PentahoSystem.init();
    }

    // We need to prime Mondrian.
    String solutionPath =
        PentahoSystem.getApplicationContext().getSolutionPath( "system/mondrian/mondrian.properties" );
    try {
      MondrianProperties.instance().load( new FileInputStream( new File( solutionPath ) ) );
    } catch ( IOException e ) {
      logger.error( "Error loading mondrian properties", e );
    }

    try {
      ( (DefaultFileSystemManager) VFS.getManager() ).addProvider( "mtm", new MetadataToMondrianVfs() );
    } catch ( FileSystemException e ) {
      if ( e.getCode().equals( "vfs.impl/multiple-providers-for-scheme.error" ) ) {
        // it's already registered. just log it as info
        logger.error( "There is already a vfs provider registered for scheme mtm", e );
      } else {
        logger.error( "There is already a vfs provider registered for scheme mtm", e );
      }
    }

  }

  @Override public void stop( BundleContext bundleContext ) throws Exception {

  }
}
