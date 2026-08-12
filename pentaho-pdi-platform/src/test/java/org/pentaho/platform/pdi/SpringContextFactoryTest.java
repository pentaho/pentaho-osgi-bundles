package org.pentaho.platform.pdi;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpringContextFactoryTest {

  /**
   * Bundle entry URLs use the OSGi-only {@code bundle:} protocol, which a plain JVM cannot parse, so
   * these tests use {@code file:} URLs whose path has the same shape.
   */
  private static URL url( String spec ) {
    try {
      return new URL( spec );
    } catch ( MalformedURLException e ) {
      throw new IllegalArgumentException( e );
    }
  }

  private static Enumeration<URL> entries( URL... urls ) {
    return new Vector<>( asList( urls ) ).elements();
  }

  @Test
  public void listSpringXmlsReturnsClasspathRelativeLocations() {
    Bundle bundle = mock( Bundle.class );
    when( bundle.findEntries( eq( "META-INF/spring" ), eq( "*.xml" ), anyBoolean() ) )
        .thenReturn( entries( url( "file:/karaf/bundle42/META-INF/spring/plugin.spring.xml" ) ) );

    List<String> locations = SpringContextFactory.listSpringXmls( bundle );

    // The leading bundle-specific path must be stripped so the composite class loader can resolve it.
    assertEquals( Collections.singletonList( "META-INF/spring/plugin.spring.xml" ), locations );
  }

  @Test
  public void listSpringXmlsSortsForDeterministicOrder() {
    Bundle bundle = mock( Bundle.class );
    when( bundle.findEntries( anyString(), anyString(), anyBoolean() ) )
        .thenReturn( entries(
            url( "file:/karaf/bundle42/META-INF/spring/z-last.xml" ),
            url( "file:/karaf/bundle42/META-INF/spring/a-first.xml" ) ) );

    assertEquals( asList( "META-INF/spring/a-first.xml", "META-INF/spring/z-last.xml" ),
        SpringContextFactory.listSpringXmls( bundle ) );
  }

  @Test
  public void listSpringXmlsReturnsEmptyWhenBundleHasNoSpringXml() {
    Bundle bundle = mock( Bundle.class );
    // findEntries returns null (not an empty enumeration) when nothing matches.
    when( bundle.findEntries( anyString(), anyString(), anyBoolean() ) ).thenReturn( null );

    assertTrue( SpringContextFactory.listSpringXmls( bundle ).isEmpty() );
  }

  @Test
  public void listSpringXmlsKeepsPathWhenMarkerDirectoryIsAbsent() {
    Bundle bundle = mock( Bundle.class );
    when( bundle.findEntries( anyString(), anyString(), anyBoolean() ) )
        .thenReturn( entries( url( "file:/somewhere/else/beans.xml" ) ) );

    assertEquals( Collections.singletonList( "/somewhere/else/beans.xml" ),
        SpringContextFactory.listSpringXmls( bundle ) );
  }

  /**
   * A plugin with no Spring XML must fail loudly rather than produce an empty context, because the
   * Blueprint that declares this factory expects a usable {@code spring} bean.
   */
  @Test
  public void createForBundleRejectsBundleWithoutSpringXml() {
    Bundle bundle = mock( Bundle.class );
    when( bundle.getSymbolicName() ).thenReturn( "no-spring-plugin" );
    when( bundle.findEntries( anyString(), anyString(), anyBoolean() ) ).thenReturn( null );

    try {
      SpringContextFactory.createForBundle( bundle );
      fail( "expected IllegalStateException" );
    } catch ( IllegalStateException expected ) {
      assertTrue( expected.getMessage().contains( "no-spring-plugin" ) );
      assertTrue( expected.getMessage().contains( "META-INF/spring" ) );
    }
  }

  /**
   * An unresolved bundle has no {@link BundleWiring}; failing here gives a clearer message than a
   * later NullPointerException.
   */
  @Test
  public void createForBundleRejectsUnresolvedBundle() {
    Bundle bundle = mock( Bundle.class );
    when( bundle.getSymbolicName() ).thenReturn( "unresolved-plugin" );
    when( bundle.findEntries( anyString(), anyString(), anyBoolean() ) )
        .thenReturn( entries( url( "file:/karaf/bundle7/META-INF/spring/plugin.spring.xml" ) ) );
    when( bundle.adapt( BundleWiring.class ) ).thenReturn( null );

    try {
      SpringContextFactory.createForBundle( bundle );
      fail( "expected IllegalStateException" );
    } catch ( IllegalStateException expected ) {
      assertTrue( expected.getMessage().contains( "unresolved-plugin" ) );
      assertTrue( expected.getMessage().contains( "not resolved" ) );
    }
  }

  /**
   * End-to-end success path: builds a real {@link ConfigurableApplicationContext} from an actual Spring
   * XML file on the test classpath, resolving one {@code plugin:} location through the bundle (proving
   * the custom {@code ProtocolResolver} works) and failing to resolve a second one (proving the 'not
   * found' branch of {@code findBundleEntry}/the warning log is safe and simply falls through to
   * default resource loading, since the bean tolerates a missing resource).
   */
  @Test
  public void createForBundleBuildsAWorkingContextAndResolvesThePluginProtocol() throws Exception {
    ClassLoader testClassLoader = getClass().getClassLoader();
    URL springXml = testClassLoader.getResource( "META-INF/spring/spring-context-factory-plugin.xml" );
    URL foundProperties = testClassLoader.getResource( "spring-context-factory-found.properties" );
    assertNotNull("test fixture spring-context-factory-plugin.xml must be on the test classpath", springXml);
    assertNotNull("test fixture spring-context-factory-found.properties must be on the test classpath", foundProperties);

    Bundle bundle = mock( Bundle.class );
    when( bundle.getSymbolicName() ).thenReturn( "spring-context-factory-test" );
    when( bundle.findEntries( eq( "META-INF/spring" ), eq( "*.xml" ), anyBoolean() ) )
        .thenReturn( entries( springXml ) );
    when( bundle.findEntries( "/", "spring-context-factory-found.properties", true ) )
        .thenReturn( entries( foundProperties ) );
    // "spring-context-factory-missing.properties" is deliberately left unstubbed - findEntries/getEntry
    // default to null/empty, so findBundleEntry cannot resolve it and must return null.

    BundleWiring wiring = mock( BundleWiring.class );
    when( wiring.getClassLoader() ).thenReturn( testClassLoader );
    when( bundle.adapt( BundleWiring.class ) ).thenReturn( wiring );

    ConfigurableApplicationContext context = SpringContextFactory.createForBundle( bundle );
    try {
      assertTrue( context.isActive() );
      assertTrue( "context must use the CompositeClassLoader built for the bundle",
          context.getClassLoader() instanceof CompositeClassLoader );

      Properties props = context.getBean( "props", Properties.class );
      assertEquals( "hello-from-plugin-protocol", props.getProperty( "greeting" ) );
    } finally {
      context.close();
    }

    assertFalse( context.isActive() );
  }
}
