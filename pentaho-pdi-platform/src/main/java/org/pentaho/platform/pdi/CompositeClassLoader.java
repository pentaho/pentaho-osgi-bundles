package org.pentaho.platform.pdi;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * A class loader that looks up classes and resources in a primary class loader first and falls back to
 * a secondary one, merging the results of {@link #getResources(String)}.
 *
 * <p>[PDI-20686] OSGi-deployed Pentaho platform plugins need to see both their own bundle content and
 * what PDI ships in {@code lib/}, which is only reachable through the class loader that launched Karaf.</p>
 *
 * <p>Merging {@link #getResources(String)} is the important part. Spring discovers its XML namespace
 * handlers by enumerating <em>every</em> {@code META-INF/spring.handlers} and
 * {@code META-INF/spring.schemas} visible to the bean class loader, so a plugin's Spring XML using
 * {@code util:} or {@code context:} would fail to parse if only the bundle's own copies were visible.
 * Observed fall-throughs to the secondary loader in a running PDI include the Spring XSDs and
 * {@code META-INF/services/org.apache.commons.logging.LogFactory}.</p>
 */
class CompositeClassLoader extends ClassLoader {

  private final ClassLoader primary;
  private final ClassLoader secondary;

  CompositeClassLoader( ClassLoader primary, ClassLoader secondary ) {
    super( primary );
    this.primary = primary;
    this.secondary = secondary;
  }

  @Override
  protected Class<?> findClass( String name ) throws ClassNotFoundException {
    return secondary.loadClass( name );
  }

  @Override
  public URL getResource( String name ) {
    URL url = primary.getResource( name );
    return url != null ? url : secondary.getResource( name );
  }

  @Override
  public Enumeration<URL> getResources( String name ) throws IOException {
    List<URL> urls = new ArrayList<>();
    addAll( urls, primary.getResources( name ) );
    addAll( urls, secondary.getResources( name ) );
    return Collections.enumeration( urls );
  }

  private static void addAll( List<URL> target, Enumeration<URL> source ) {
    while ( source != null && source.hasMoreElements() ) {
      URL url = source.nextElement();
      if ( !target.contains( url ) ) {
        target.add( url );
      }
    }
  }
}
