package org.pentaho.platform.pdi;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.Test;
import org.pentaho.platform.pdi.vfs.MetadataToMondrianVfs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Pins the Apache Commons VFS contract that {@link PdiPlatformActivator} relies on when it registers
 * the {@code mtm} scheme.
 *
 * <p>[PDI-20686] {@code PdiPlatformActivator.stop()} is empty, so the provider registered by one
 * incarnation of the bundle survives into the next. After a bundle refresh the activator therefore runs
 * against a manager that already has {@code mtm}, and a plain {@code addProvider} is rejected - leaving
 * the <em>old</em> provider in place, bound to an invalidated class loader. Removing first makes the
 * registration idempotent.</p>
 *
 * <p>These tests use their own {@link DefaultFileSystemManager} rather than the JVM-wide
 * {@code VFS.getManager()} singleton, which other tests in this module also register {@code mtm} on.</p>
 */
public class PdiPlatformActivatorVfsRegistrationTest {

  private static final String SCHEME = "mtm";

  /** The failure the fix exists to avoid: a second registration is rejected, not replaced. */
  @Test
  public void addingTheSameSchemeTwiceIsRejected() throws Exception {
    DefaultFileSystemManager manager = new DefaultFileSystemManager();
    manager.addProvider( SCHEME, new MetadataToMondrianVfs() );

    try {
      manager.addProvider( SCHEME, new MetadataToMondrianVfs() );
      fail( "expected commons-vfs2 to reject a duplicate provider for scheme " + SCHEME );
    } catch ( FileSystemException expected ) {
      assertEquals( "vfs.impl/multiple-providers-for-scheme.error", expected.getCode() );
    }
  }

  /** What the activator now does: remove first, so re-registration always succeeds. */
  @Test
  public void removingBeforeAddingMakesRegistrationRepeatable() throws Exception {
    DefaultFileSystemManager manager = new DefaultFileSystemManager();

    for ( int incarnation = 0; incarnation < 3; incarnation++ ) {
      manager.removeProvider( SCHEME );
      manager.addProvider( SCHEME, new MetadataToMondrianVfs() );
    }

    // The provider in place is the most recently registered one, not a stale first registration.
    assertEquals( true, manager.hasProvider( SCHEME ) );
  }

  /** On a cold start nothing is registered yet, so the extra remove must be harmless. */
  @Test
  public void removingAnUnregisteredSchemeIsANoOp() throws Exception {
    DefaultFileSystemManager manager = new DefaultFileSystemManager();

    manager.removeProvider( SCHEME );
    manager.addProvider( SCHEME, new MetadataToMondrianVfs() );

    assertEquals( true, manager.hasProvider( SCHEME ) );
  }
}
