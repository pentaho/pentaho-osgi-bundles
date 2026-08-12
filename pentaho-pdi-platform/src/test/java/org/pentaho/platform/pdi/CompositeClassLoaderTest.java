package org.pentaho.platform.pdi;

import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CompositeClassLoaderTest {

  private static URL url( String spec ) {
    try {
      return new URL( spec );
    } catch ( MalformedURLException e ) {
      throw new IllegalArgumentException( e );
    }
  }

  /** Class loader that serves a fixed set of resources and can load one named class. */
  private static class StubClassLoader extends ClassLoader {
    private final List<URL> resources;
    private final String loadableClassName;

    StubClassLoader( List<URL> resources, String loadableClassName ) {
      super( null );
      this.resources = resources;
      this.loadableClassName = loadableClassName;
    }

    @Override
    public URL getResource( String name ) {
      return resources.isEmpty() ? null : resources.get( 0 );
    }

    @Override
    public Enumeration<URL> getResources( String name ) {
      return Collections.enumeration( resources );
    }

    // findClass (not loadClass) so that the standard parent-first delegation of ClassLoader applies.
    @Override
    protected Class<?> findClass( String name ) throws ClassNotFoundException {
      if ( name.equals( loadableClassName ) ) {
        return String.class;
      }
      throw new ClassNotFoundException( name );
    }
  }

  @Test
  public void getResourcePrefersPrimary() {
    URL primaryUrl = url( "file:/primary/spring.handlers" );
    URL secondaryUrl = url( "file:/secondary/spring.handlers" );

    CompositeClassLoader loader = new CompositeClassLoader(
        new StubClassLoader( Collections.singletonList( primaryUrl ), null ),
        new StubClassLoader( Collections.singletonList( secondaryUrl ), null ) );

    assertEquals( primaryUrl, loader.getResource( "META-INF/spring.handlers" ) );
  }

  @Test
  public void getResourceFallsBackToSecondary() {
    URL secondaryUrl = url( "file:/secondary/spring.schemas" );

    CompositeClassLoader loader = new CompositeClassLoader(
        new StubClassLoader( Collections.emptyList(), null ),
        new StubClassLoader( Collections.singletonList( secondaryUrl ), null ) );

    assertEquals( secondaryUrl, loader.getResource( "META-INF/spring.schemas" ) );
  }

  @Test
  public void getResourceReturnsNullWhenNeitherHasIt() {
    CompositeClassLoader loader = new CompositeClassLoader(
        new StubClassLoader( Collections.emptyList(), null ),
        new StubClassLoader( Collections.emptyList(), null ) );

    assertNull( loader.getResource( "META-INF/absent" ) );
  }

  /**
   * The reason this class exists: Spring locates its namespace handlers by enumerating every
   * {@code META-INF/spring.handlers} on the class path, so both loaders' entries must be visible.
   */
  @Test
  public void getResourcesMergesBothClassLoaders() throws IOException {
    URL primaryUrl = url( "file:/primary/spring.handlers" );
    URL secondaryUrl = url( "file:/secondary/spring.handlers" );

    CompositeClassLoader loader = new CompositeClassLoader(
        new StubClassLoader( Collections.singletonList( primaryUrl ), null ),
        new StubClassLoader( Collections.singletonList( secondaryUrl ), null ) );

    List<URL> found = Collections.list( loader.getResources( "META-INF/spring.handlers" ) );

    assertEquals( 2, found.size() );
    assertTrue( found.contains( primaryUrl ) );
    assertTrue( found.contains( secondaryUrl ) );
  }

  @Test
  public void getResourcesDeduplicatesIdenticalUrls() throws IOException {
    URL shared = url( "file:/shared/spring.handlers" );

    CompositeClassLoader loader = new CompositeClassLoader(
        new StubClassLoader( Collections.singletonList( shared ), null ),
        new StubClassLoader( Collections.singletonList( shared ), null ) );

    assertEquals( 1, Collections.list( loader.getResources( "META-INF/spring.handlers" ) ).size() );
  }

  @Test
  public void getResourcesToleratesEmptyEnumerations() throws IOException {
    CompositeClassLoader loader = new CompositeClassLoader(
        new StubClassLoader( Collections.emptyList(), null ),
        new StubClassLoader( Collections.emptyList(), null ) );

    assertTrue( Collections.list( loader.getResources( "META-INF/none" ) ).isEmpty() );
  }

  @Test
  public void getResourcesPreservesPrimaryOrderFirst() throws IOException {
    URL primaryUrl = url( "file:/primary/x" );
    URL secondaryUrl = url( "file:/secondary/x" );

    CompositeClassLoader loader = new CompositeClassLoader(
        new StubClassLoader( Collections.singletonList( primaryUrl ), null ),
        new StubClassLoader( Collections.singletonList( secondaryUrl ), null ) );

    assertEquals( asList( primaryUrl, secondaryUrl ),
        Collections.list( loader.getResources( "x" ) ) );
  }

  /**
   * {@code findClass} is only consulted after the parent (the primary loader) has failed, so it must
   * delegate to the secondary loader.
   */
  @Test
  public void findClassDelegatesToSecondary() throws ClassNotFoundException {
    CompositeClassLoader loader = new CompositeClassLoader(
        new StubClassLoader( Collections.emptyList(), null ),
        new StubClassLoader( Collections.emptyList(), "some.Type" ) );

    assertSame( String.class, loader.findClass( "some.Type" ) );
  }

  @Test
  public void findClassPropagatesClassNotFound() {
    CompositeClassLoader loader = new CompositeClassLoader(
        new StubClassLoader( Collections.emptyList(), null ),
        new StubClassLoader( Collections.emptyList(), null ) );

    try {
      loader.findClass( "absent.Type" );
      fail( "expected ClassNotFoundException" );
    } catch ( ClassNotFoundException expected ) {
      assertEquals( "absent.Type", expected.getMessage() );
    }
  }

  @Test
  public void primaryIsTheParentSoNormalDelegationApplies() throws Exception {
    StubClassLoader primary = new StubClassLoader( Collections.emptyList(), "primary.Type" );

    CompositeClassLoader loader = new CompositeClassLoader(
        primary, new StubClassLoader( Collections.emptyList(), null ) );

    // loadClass -> parent (primary) resolves it, findClass is never reached
    assertSame( String.class, loader.loadClass( "primary.Type" ) );
  }

  @Test
  public void getResourcesHandlesNullEnumerationFromClassLoader() throws IOException {
    ClassLoader nullReturning = new ClassLoader( null ) {
      @Override
      public Enumeration<URL> getResources( String name ) {
        return null;
      }
    };
    URL secondaryUrl = url( "file:/secondary/y" );

    CompositeClassLoader loader = new CompositeClassLoader(
        nullReturning, new StubClassLoader( new Vector<>( Collections.singletonList( secondaryUrl ) ), null ) );

    assertEquals( Collections.singletonList( secondaryUrl ),
        Collections.list( loader.getResources( "y" ) ) );
  }
}
