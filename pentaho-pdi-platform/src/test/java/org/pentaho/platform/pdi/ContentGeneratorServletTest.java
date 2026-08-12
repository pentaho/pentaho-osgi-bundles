package org.pentaho.platform.pdi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Vector;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContentGeneratorServletTest {

  private static final String BEAN_ID = "xanalyzer.service";

  private ApplicationContext applicationContext;
  private IContentGenerator contentGenerator;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private ContentGeneratorServlet servlet;

  @Before
  public void setUp() throws Exception {
    contentGenerator = mock( IContentGenerator.class );
    applicationContext = mock( ApplicationContext.class );
    when( applicationContext.getBean( BEAN_ID ) ).thenReturn( contentGenerator );
    when( applicationContext.getClassLoader() ).thenReturn( getClass().getClassLoader() );

    request = mock( HttpServletRequest.class );
    when( request.getParameterNames() ).thenReturn( new Vector<String>().elements() );
    when( request.getHeaderNames() ).thenReturn( new Vector<String>().elements() );
    when( request.getInputStream() ).thenReturn( mock( ServletInputStream.class ) );

    response = mock( HttpServletResponse.class );
    when( response.getOutputStream() ).thenReturn( mock( ServletOutputStream.class ) );

    servlet = new ContentGeneratorServlet( applicationContext, BEAN_ID );
  }

  @SuppressWarnings( "unchecked" )
  private Map<String, IParameterProvider> captureParameterProviders() throws Exception {
    servlet.service( request, response );
    ArgumentCaptor<Map<String, IParameterProvider>> captor = ArgumentCaptor.forClass( Map.class );
    verify( contentGenerator ).setParameterProviders( captor.capture() );
    return captor.getValue();
  }

  @Test
  public void drivesTheContentGeneratorDirectly() throws Exception {
    servlet.service( request, response );

    // The generator is driven through IOutputHandler rather than GeneratorStreamingOutput, which would
    // pass servlet types across the OSGi/lib class loader boundary and throw a LinkageError.
    verify( contentGenerator ).setOutputHandler( org.mockito.ArgumentMatchers.any() );
    verify( contentGenerator ).setParameterProviders( org.mockito.ArgumentMatchers.any() );
    verify( contentGenerator ).setSession( org.mockito.ArgumentMatchers.any() );
    verify( contentGenerator ).createContent();
  }

  @Test
  public void restoresTheContextClassLoaderAfterwards() throws Exception {
    ClassLoader before = Thread.currentThread().getContextClassLoader();

    servlet.service( request, response );

    assertSame( before, Thread.currentThread().getContextClassLoader() );
  }

  @Test
  public void restoresTheContextClassLoaderEvenWhenTheGeneratorFails() throws Exception {
    ClassLoader before = Thread.currentThread().getContextClassLoader();
    org.mockito.Mockito.doThrow( new IllegalStateException( "boom" ) )
        .when( contentGenerator ).createContent();

    try {
      servlet.service( request, response );
      fail( "expected ServletException" );
    } catch ( ServletException expected ) {
      assertTrue( expected.getMessage().contains( BEAN_ID ) );
    }

    assertSame( before, Thread.currentThread().getContextClassLoader() );
  }

  @Test
  public void setsACompositeContextClassLoaderWhileGenerating() throws Exception {
    ClassLoader[] seen = new ClassLoader[ 1 ];
    org.mockito.Mockito.doAnswer( invocation -> {
      seen[ 0 ] = Thread.currentThread().getContextClassLoader();
      return null;
    } ).when( contentGenerator ).createContent();

    servlet.service( request, response );

    assertNotNull( seen[ 0 ] );
    assertTrue( "expected a CompositeClassLoader, got " + seen[ 0 ].getClass(),
        seen[ 0 ] instanceof CompositeClassLoader );
  }

  @Test
  public void exposesRequestParametersInTheRequestScope() throws Exception {
    when( request.getParameterNames() )
        .thenReturn( new Vector<>( asList( "catalog", "cube" ) ).elements() );
    when( request.getParameter( "catalog" ) ).thenReturn( "SteelWheels" );
    when( request.getParameter( "cube" ) ).thenReturn( "Sales" );

    IParameterProvider scope = captureParameterProviders().get( IParameterProvider.SCOPE_REQUEST );

    assertEquals( "SteelWheels", scope.getStringParameter( "catalog", null ) );
    assertEquals( "Sales", scope.getStringParameter( "cube", null ) );
  }

  @Test
  public void exposesRequestHeadersInTheHeadersScope() throws Exception {
    when( request.getHeaderNames() )
        .thenReturn( new Vector<>( asList( "Accept" ) ).elements() );
    when( request.getHeader( "Accept" ) ).thenReturn( "application/json" );

    IParameterProvider scope = captureParameterProviders().get( "headers" );

    assertEquals( "application/json", scope.getStringParameter( "Accept", null ) );
  }

  @Test
  public void exposesTheRequestAndResponseInThePathScope() throws Exception {
    IParameterProvider path = captureParameterProviders().get( "path" );

    assertSame( request, path.getParameter( "httprequest" ) );
    assertSame( response, path.getParameter( "httpresponse" ) );
  }

  @Test
  public void providesAllScopesTheContentGeneratorsExpect() throws Exception {
    Map<String, IParameterProvider> providers = captureParameterProviders();

    assertTrue( providers.containsKey( IParameterProvider.SCOPE_REQUEST ) );
    assertTrue( providers.containsKey( IParameterProvider.SCOPE_SESSION ) );
    assertTrue( providers.containsKey( "headers" ) );
    assertTrue( providers.containsKey( "path" ) );
  }

  /** The command is what the Analyzer content generator dispatches on. */
  @Test
  public void commandIsThePathBelowTheWhiteboardPattern() throws Exception {
    when( request.getPathInfo() ).thenReturn( "/ping" );

    assertEquals( "ping", captureParameterProviders().get( "path" ).getStringParameter( "cmd", null ) );
  }

  @Test
  public void commandFallsBackToTheBeanLocalNameWhenPathInfoIsNull() throws Exception {
    when( request.getPathInfo() ).thenReturn( null );

    assertEquals( "service", captureParameterProviders().get( "path" ).getStringParameter( "cmd", null ) );
  }

  @Test
  public void commandFallsBackToTheBeanLocalNameForThePatternRoot() throws Exception {
    when( request.getPathInfo() ).thenReturn( "/" );

    assertEquals( "service", captureParameterProviders().get( "path" ).getStringParameter( "cmd", null ) );
  }

  @Test
  public void commandFallsBackToTheBeanLocalNameForAnEmptyPathInfo() throws Exception {
    when( request.getPathInfo() ).thenReturn( "" );

    assertEquals( "service", captureParameterProviders().get( "path" ).getStringParameter( "cmd", null ) );
  }

  @Test
  public void commandKeepsNestedPaths() throws Exception {
    when( request.getPathInfo() ).thenReturn( "/api/report/run" );

    assertEquals( "api/report/run",
        captureParameterProviders().get( "path" ).getStringParameter( "cmd", null ) );
  }

  @Test
  public void beanIdWithoutADotStillYieldsACommand() throws Exception {
    ContentGeneratorServlet plainBean = new ContentGeneratorServlet( applicationContext, "geojson" );
    when( applicationContext.getBean( "geojson" ) ).thenReturn( contentGenerator );
    when( request.getPathInfo() ).thenReturn( null );

    plainBean.service( request, response );

    ArgumentCaptor<Map<String, IParameterProvider>> captor = ArgumentCaptor.forClass( Map.class );
    verify( contentGenerator ).setParameterProviders( captor.capture() );
    assertEquals( "geojson", captor.getValue().get( "path" ).getStringParameter( "cmd", null ) );
  }

  @Test
  public void wrapsGeneratorFailuresAsServletException() throws Exception {
    org.mockito.Mockito.doThrow( new IllegalStateException( "kaboom" ) )
        .when( contentGenerator ).createContent();

    try {
      servlet.service( request, response );
      fail( "expected ServletException" );
    } catch ( ServletException e ) {
      assertTrue( e.getMessage().contains( BEAN_ID ) );
      assertEquals( "kaboom", e.getCause().getMessage() );
    }
  }

  /**
   * [PDI-20686] {@code ApplicationContext.getClassLoader()} is documented as nullable. Without a
   * fallback, {@code CompositeClassLoader} would be built with a null primary delegate and throw an
   * NPE the first time a content generator touches the class loader (e.g. resolving a resource).
   */
  @Test
  public void toleratesANullApplicationContextClassLoader() throws Exception {
    when( applicationContext.getClassLoader() ).thenReturn( null );

    ClassLoader[] seen = new ClassLoader[ 1 ];
    org.mockito.Mockito.doAnswer( invocation -> {
      seen[ 0 ] = Thread.currentThread().getContextClassLoader();
      // Would NPE here if the composite's primary delegate were null.
      seen[ 0 ].getResource( "does/not/exist" );
      return null;
    } ).when( contentGenerator ).createContent();

    servlet.service( request, response );

    assertNotNull( seen[ 0 ] );
    assertTrue( seen[ 0 ] instanceof CompositeClassLoader );
  }

  /**
   * [PDI-20686] {@code Thread.getContextClassLoader()} is documented as nullable (e.g. a thread that
   * never had one set). Without a fallback, {@code CompositeClassLoader} would be built with a null
   * secondary delegate and throw an NPE the first time a content generator loads a class through it.
   */
  @Test
  public void toleratesANullThreadContextClassLoader() throws Exception {
    ClassLoader[] seen = new ClassLoader[ 1 ];
    org.mockito.Mockito.doAnswer( invocation -> {
      seen[ 0 ] = Thread.currentThread().getContextClassLoader();
      // getResource() bypasses normal parent-delegation and always falls through to the secondary
      // delegate when the primary misses, so this reliably exercises the secondary null-guard - unlike
      // loadClass(), which would resolve via the (non-null) primary/parent before ever reaching it.
      seen[ 0 ].getResource( "does/not/exist" );
      return null;
    } ).when( contentGenerator ).createContent();

    Thread thread = new Thread( () -> {
      try {
        servlet.service( request, response );
      } catch ( Exception e ) {
        throw new RuntimeException( e );
      }
    } );
    thread.setContextClassLoader( null );
    thread.start();
    thread.join();

    assertNotNull( seen[ 0 ] );
    assertTrue( seen[ 0 ] instanceof CompositeClassLoader );
  }
}
