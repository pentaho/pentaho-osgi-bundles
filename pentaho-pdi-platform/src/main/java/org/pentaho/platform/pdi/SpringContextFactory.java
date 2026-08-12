package org.pentaho.platform.pdi;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.UrlResource;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Builds a Spring {@link ConfigurableApplicationContext} for a Pentaho platform plugin bundle from the
 * {@code META-INF/spring/*.xml} files it ships.
 *
 * <p>[PDI-20686] This restores the one service Spring DM (Spring OSGi 1.2.1) used to provide for platform
 * plugins, which was removed by SP-6858 along with the CVE-bearing Spring 3.2.18. A plugin asks for its
 * own context from its own Blueprint, so no extender bundle and no OSGi service handshake are involved:</p>
 *
 * <pre>
 * &lt;bean id="spring" class="org.pentaho.platform.pdi.SpringContextFactory"
 *       factory-method="createForBundle" destroy-method="close"&gt;
 *   &lt;argument ref="blueprintBundle"/&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * <p>Because the context is an ordinary bean of the plugin's own Blueprint container, it is created and
 * destroyed with that container, and the servlets that {@code SpringFileHandler} generates (which
 * reference the same {@code spring} id) cannot be published before it exists.</p>
 *
 * <p>Two pieces of legacy behavior are reproduced so that <strong>unmodified</strong> plugin Spring XML
 * keeps working:</p>
 * <ol>
 *   <li>A {@link CompositeClassLoader} that resolves against the plugin bundle first and the class loader
 *       that provides Spring second. Crucially {@code getResources} is <em>merged</em> across both, so all
 *       {@code META-INF/spring.handlers} and {@code META-INF/spring.schemas} entries are visible &mdash;
 *       that is what makes namespaces such as {@code util:} and {@code context:} resolve.</li>
 *   <li>A {@code plugin:} resource protocol (the old {@code PentahoOsgiBundleXmlApplicationContext}
 *       behavior), so legacy references such as {@code value="plugin:analyzer.properties"} resolve to the
 *       matching bundle entry.</li>
 * </ol>
 */
public final class SpringContextFactory {

  private static final Logger logger = LoggerFactory.getLogger( SpringContextFactory.class );

  private static final String SPRING_XML_DIR = "META-INF/spring";
  private static final String SPRING_XML_PATTERN = "*.xml";
  private static final String PLUGIN_PROTOCOL = "plugin:";

  private SpringContextFactory() {
  }

  /**
   * Create and refresh a Spring ApplicationContext from the given bundle's {@code META-INF/spring/*.xml}.
   *
   * @param bundle the plugin bundle, normally supplied as Blueprint's predefined {@code blueprintBundle}
   * @return a refreshed context; close it through {@code destroy-method="close"}
   */
  public static ConfigurableApplicationContext createForBundle( final Bundle bundle ) {
    String[] configLocations = listSpringXmls( bundle ).toArray( new String[ 0 ] );
    if ( configLocations.length == 0 ) {
      throw new IllegalStateException(
          "Bundle " + bundle.getSymbolicName() + " has no " + SPRING_XML_DIR + "/" + SPRING_XML_PATTERN );
    }

    BundleWiring wiring = bundle.adapt( BundleWiring.class );
    if ( wiring == null ) {
      throw new IllegalStateException( "Bundle " + bundle.getSymbolicName() + " has no wiring (not resolved)" );
    }

    // Spring is exported to OSGi as system packages, so the class loader that defines Spring core here is
    // the one that also carries META-INF/spring.handlers and META-INF/spring.schemas.
    ClassLoader springClassLoader = ClassPathXmlApplicationContext.class.getClassLoader();
    final ClassLoader compositeClassLoader = new CompositeClassLoader( wiring.getClassLoader(), springClassLoader );

    if ( logger.isInfoEnabled() ) {
      logger.info( "Creating Spring ApplicationContext for bundle '{}' from {}",
          bundle.getSymbolicName(), Arrays.toString( configLocations ) );
    }

    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext( configLocations, false );
    context.setClassLoader( compositeClassLoader );
    // Restore the legacy 'plugin:' protocol without subclassing the context: subclassing triggers a
    // LinkageError when the bundle's view of org.springframework.core.io.Resource and the superclass'
    // view come from different class loaders. A ProtocolResolver is loaded through the context's own
    // class loader, so it is loader-constraint safe.
    context.addProtocolResolver( ( location, resourceLoader ) -> {
      if ( location.startsWith( PLUGIN_PROTOCOL ) ) {
        URL found = findBundleEntry( bundle, location.substring( PLUGIN_PROTOCOL.length() ) );
        if ( found != null ) {
          return new UrlResource( found );
        }
        logger.warn( "Could not resolve '{}' in bundle '{}'", location, bundle.getSymbolicName() );
      }
      return null;
    } );

    // Only refresh() loads classes and parses the XML, so only it runs under the composite class loader. The
    // context is deliberately not closed here: it is the factory's result, and Blueprint closes it through
    // destroy-method="close" when the plugin's container is destroyed. That is also why this is a plain
    // try/finally that restores the class loader, not a try-with-resources on the context.
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader( compositeClassLoader );
    try {
      context.refresh();
    } finally {
      Thread.currentThread().setContextClassLoader( originalClassLoader );
    }

    logger.info( "Spring ApplicationContext ready for bundle '{}'", bundle.getSymbolicName() );
    return context;
  }

  /**
   * List the bundle's Spring XML files as classpath-relative locations, sorted for a deterministic order.
   */
  static List<String> listSpringXmls( Bundle bundle ) {
    List<String> result = new ArrayList<>();
    Enumeration<URL> entries = bundle.findEntries( SPRING_XML_DIR, SPRING_XML_PATTERN, false );
    while ( entries != null && entries.hasMoreElements() ) {
      String path = entries.nextElement().getPath();
      int idx = path.indexOf( SPRING_XML_DIR );
      result.add( idx >= 0 ? path.substring( idx ) : path );
    }
    Collections.sort( result );
    return result;
  }

  /**
   * Resolve a {@code plugin:}-relative resource to a bundle entry. Exact paths win, so that
   * {@code plugin:a/config.properties} and {@code plugin:b/config.properties} never resolve to the same file:
   * <ol>
   *   <li>{@code <Bundle-SymbolicName>/<path>} - what the legacy {@code PentahoOsgiBundleXmlApplicationContext}
   *       resolved (e.g. {@code analyzer.properties} -&gt; {@code analyzer/analyzer.properties});</li>
   *   <li>{@code <path>} from the bundle root;</li>
   *   <li>only then, the first entry anywhere in the bundle with the same file name.</li>
   * </ol>
   */
  private static URL findBundleEntry( Bundle bundle, String relativePath ) {
    String path = relativePath.startsWith( "/" ) ? relativePath.substring( 1 ) : relativePath;

    URL exact = bundle.getEntry( bundle.getSymbolicName() + "/" + path );
    if ( exact == null ) {
      exact = bundle.getEntry( path );
    }
    if ( exact != null ) {
      return exact;
    }

    String fileName = path.substring( path.lastIndexOf( '/' ) + 1 );
    Enumeration<URL> entries = bundle.findEntries( "/", fileName, true );
    return entries != null && entries.hasMoreElements() ? entries.nextElement() : null;
  }
}
