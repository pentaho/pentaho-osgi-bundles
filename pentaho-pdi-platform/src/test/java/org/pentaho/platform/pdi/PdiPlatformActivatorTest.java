package org.pentaho.platform.pdi;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.VFS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.plugin.services.pluginmgr.PentahoSystemPluginManager;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Exercises {@link PdiPlatformActivator#start(BundleContext)}, which drives real (static)
 * {@link PentahoSystem} state rather than a mockable collaborator, so these tests manage that shared,
 * process-wide state directly: each test starts from a clean {@code PentahoSystem.shutdown()} and ends
 * with one, so tests don't leak registrations or an initialized status into each other or into other
 * test classes sharing this JVM/fork.
 */
public class PdiPlatformActivatorTest {

  private File solutionRoot;

  @Before
  public void setUp() throws Exception {
    PentahoSystem.shutdown();
    solutionRoot = File.createTempFile( "PdiPlatformActivatorTest", "" );
    assertTrue( solutionRoot.delete() );
    File mondrianDir = new File( solutionRoot, "system/mondrian" );
    assertTrue( mondrianDir.mkdirs() );
    FileUtils.write( new File( mondrianDir, "mondrian.properties" ), "mondrian.olap.PrecacheHierarchyAll=false\n", StandardCharsets.UTF_8 );
  }

  @After
  public void tearDown() throws Exception {
    PentahoSystem.shutdown();
    if ( solutionRoot != null ) {
      FileUtils.deleteDirectory( solutionRoot );
    }
  }

  /** A cold start: nothing registered yet, PentahoSystem not initialized. */
  @Test
  public void startRegistersDefaultsAndBootsPentahoSystemOnAColdStart() throws Exception {
    PentahoSystem.setApplicationContext( new StandaloneApplicationContext( solutionRoot.getAbsolutePath(),
        solutionRoot.getAbsolutePath() ) );
    assertEquals( PentahoSystem.SYSTEM_NOT_INITIALIZED, PentahoSystem.getInitializedStatus() );

    new PdiPlatformActivator().start( mock( BundleContext.class ) );

    assertTrue( PentahoSystem.get( IAuthorizationPolicy.class ) instanceof AgileBiAuthorizationPolicy );
    assertSame( PdiPlatformActivator.RESOURCE_LOADER, PentahoSystem.get( IPluginResourceLoader.class ) );

    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );
    assertTrue( pluginManager instanceof PentahoSystemPluginManager );
    assertNull( pluginManager.getPluginSetting( "any-plugin", "any-key", "any-default" ) );

    // getInitializedStatus() != SYSTEM_INITIALIZED_OK, so the activator must have called PentahoSystem.init()
    // itself - but init() with no arguments builds its own StandaloneApplicationContext(".", "."), replacing
    // the one this test configured above.
    assertEquals( PentahoSystem.SYSTEM_INITIALIZED_OK, PentahoSystem.getInitializedStatus() );

    assertTrue( VFS.getManager().hasProvider( "mtm" ) );
  }

  /** Everything already registered and PentahoSystem already booted: every guard must be skipped. */
  @Test
  public void startSkipsRegistrationAndBootWhenAlreadyDone() throws Exception {
    IAuthorizationPolicy existingPolicy = mock( IAuthorizationPolicy.class );
    IPluginResourceLoader existingLoader = mock( IPluginResourceLoader.class );
    IPluginManager existingPluginManager = mock( IPluginManager.class );
    PentahoSystem.registerObject( existingPolicy, IAuthorizationPolicy.class );
    PentahoSystem.registerObject( existingLoader, IPluginResourceLoader.class );
    PentahoSystem.registerObject( existingPluginManager, IPluginManager.class );

    PentahoSystem.init( new StandaloneApplicationContext( solutionRoot.getAbsolutePath(),
        solutionRoot.getAbsolutePath() ) );
    assertEquals( PentahoSystem.SYSTEM_INITIALIZED_OK, PentahoSystem.getInitializedStatus() );

    new PdiPlatformActivator().start( mock( BundleContext.class ) );

    // Nothing the activator would have registered replaced what was already there.
    assertSame( existingPolicy, PentahoSystem.get( IAuthorizationPolicy.class ) );
    assertSame( existingLoader, PentahoSystem.get( IPluginResourceLoader.class ) );
    assertSame( existingPluginManager, PentahoSystem.get( IPluginManager.class ) );

    // The application context this test configured (pointing at a solution root with a real
    // mondrian.properties) must survive untouched - proof PentahoSystem.init() was not called again,
    // which would have replaced it with a fresh StandaloneApplicationContext(".", ".").
    assertTrue( PentahoSystem.getApplicationContext().getSolutionPath( "" ).startsWith(
        solutionRoot.getAbsolutePath() ) );

    assertTrue( VFS.getManager().hasProvider( "mtm" ) );
  }

  /** A missing/unreadable mondrian.properties must be logged, not thrown - the bundle must still start. */
  @Test
  public void startToleratesAMissingMondrianPropertiesFile() throws Exception {
    File emptySolutionRoot = new File( solutionRoot, "no-mondrian-here" );
    assertTrue( emptySolutionRoot.mkdirs() );
    PentahoSystem.init( new StandaloneApplicationContext( emptySolutionRoot.getAbsolutePath(),
        emptySolutionRoot.getAbsolutePath() ) );

    // Must not throw despite system/mondrian/mondrian.properties not existing under emptySolutionRoot.
    new PdiPlatformActivator().start( mock( BundleContext.class ) );

    assertNotNull( PentahoSystem.get( IAuthorizationPolicy.class ) );
    assertTrue( VFS.getManager().hasProvider( "mtm" ) );
  }

  /**
   * stop() is a documented no-op; calling it must not throw regardless of prior start() calls, and it
   * must not touch the bundle context it is handed - both are asserted by verifying the mock saw no
   * interactions at all.
   */
  @Test
  public void stopIsANoOp() throws Exception {
    BundleContext bundleContext = mock( BundleContext.class );

    new PdiPlatformActivator().stop( bundleContext );

    verifyNoInteractions( bundleContext );
  }

  /**
   * Re-registering the {@code mtm} VFS provider on a second {@code start()} must not fail even though a
   * previous incarnation already registered it - this is the behaviour
   * {@link PdiPlatformActivatorVfsRegistrationTest} pins directly against a private manager; here it's
   * exercised through the real activator and the real singleton {@code VFS.getManager()}.
   */
  @Test
  public void startIsRepeatableAcrossBundleRefreshes() throws Exception {
    PentahoSystem.init( new StandaloneApplicationContext( solutionRoot.getAbsolutePath(),
        solutionRoot.getAbsolutePath() ) );

    PdiPlatformActivator activator = new PdiPlatformActivator();
    activator.start( mock( BundleContext.class ) );
    activator.start( mock( BundleContext.class ) );

    assertTrue( VFS.getManager().hasProvider( "mtm" ) );
  }
}
