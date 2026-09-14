/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.pdi;

import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Bridges an OSGi-deployed Pentaho content generator (e.g. {@code AnalyzerContentGenerator}) to a
 * servlet. One instance is registered per content-generator bean by the Blueprint that
 * {@code SpringFileHandler} generates for the plugin bundle.
 *
 * <p>[PDI-20686] The generator is driven directly, through {@link IOutputHandler}, rather than through
 * {@code GeneratorStreamingOutput}. That class lives in PDI's {@code lib/} (the main class loader) and
 * its constructor takes {@code HttpServletRequest}/{@code HttpServletResponse}; this servlet lives in an
 * OSGi bundle wired to a <em>different</em> {@code javax.servlet} package, so calling it fails with a
 * {@code LinkageError} ("loader constraint violation … different Class objects for the type
 * javax/servlet/http/HttpServletResponse"). Passing only an {@code OutputStream} keeps servlet types on
 * this side of the class loader boundary.</p>
 *
 * <p>Created by nbaker on 7/25/16.</p>
 */
public class ContentGeneratorServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private final ApplicationContext applicationContext;
  private final String beanId;

  public ContentGeneratorServlet( ApplicationContext applicationContext, String beanId ) {
    this.applicationContext = applicationContext;
    this.beanId = beanId;
  }

  @Override protected void service( HttpServletRequest req, HttpServletResponse resp )
      throws ServletException, IOException {

    // We are anonymous for now
    PentahoSessionHolder.setSession( new StandaloneSession( "bob" ) );

    IContentGenerator contentGenerator = (IContentGenerator) applicationContext.getBean( beanId );

    // Delegates to the plugin bundle first and to the caller's class loader second, so content
    // generators see both their own bundle content and what PDI ships in lib/. Both class loaders are
    // guaranteed non-null before being handed to CompositeClassLoader (which dereferences them directly
    // in findClass/getResource/getResources with no null-check): getContextClassLoader() and
    // ApplicationContext.getClassLoader() are both documented as nullable, so fall back to this class'
    // own loader - guaranteed to exist since it comes from this OSGi bundle - if either is missing. The
    // true original context class loader (possibly null) is kept separately so it can be restored as-is.
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    ClassLoader secondaryClassLoader =
        originalClassLoader != null ? originalClassLoader : ContentGeneratorServlet.class.getClassLoader();
    ClassLoader primaryClassLoader = applicationContext.getClassLoader();
    if ( primaryClassLoader == null ) {
      primaryClassLoader = ContentGeneratorServlet.class.getClassLoader();
    }
    ClassLoader compositeClassLoader = new CompositeClassLoader( primaryClassLoader, secondaryClassLoader );

    Thread.currentThread().setContextClassLoader( compositeClassLoader );
    try {
      // Only an OutputStream is handed over, so no servlet type crosses into PDI's lib/ (see class javadoc).
      IOutputHandler outputHandler = new SimpleOutputHandler( resp.getOutputStream(), true );

      contentGenerator.setOutputHandler( outputHandler );
      contentGenerator.setParameterProviders( buildParameterProviders( req, resp ) );
      contentGenerator.setSession( PentahoSessionHolder.getSession() );
      contentGenerator.createContent();

    } catch ( Exception e ) {
      throw new ServletException( "Error generating content for bean " + beanId, e );
    } finally {
      Thread.currentThread().setContextClassLoader( originalClassLoader );
    }
  }

  private Map<String, IParameterProvider> buildParameterProviders( HttpServletRequest req,
                                                                   HttpServletResponse resp ) throws IOException {
    Map<String, IParameterProvider> parameterProviders = new HashMap<>();

    SimpleParameterProvider requestParams = new SimpleParameterProvider();
    Enumeration<String> paramNames = req.getParameterNames();
    while ( paramNames.hasMoreElements() ) {
      String name = paramNames.nextElement();
      requestParams.setParameter( name, req.getParameter( name ) );
    }
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParams );

    parameterProviders.put( IParameterProvider.SCOPE_SESSION, new SimpleParameterProvider() );

    SimpleParameterProvider headerParams = new SimpleParameterProvider();
    Enumeration<String> headerNames = req.getHeaderNames();
    while ( headerNames.hasMoreElements() ) {
      String name = headerNames.nextElement();
      headerParams.setParameter( name, req.getHeader( name ) );
    }
    parameterProviders.put( "headers", headerParams );

    // Content generators read the request/response and the command from the "path" scope.
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    pathParams.setParameter( "query", req.getQueryString() );
    pathParams.setParameter( "inputstream", req.getInputStream() );
    pathParams.setParameter( "httprequest", req );
    pathParams.setParameter( "httpresponse", resp );
    pathParams.setParameter( "remoteaddr", req.getRemoteAddr() );
    pathParams.setParameter( "cmd", resolveCommand( req ) );
    parameterProviders.put( "path", pathParams );

    return parameterProviders;
  }

  /**
   * The command is the path below the registered whiteboard pattern (e.g. {@code /content/analyzer/service/*}).
   * Falls back to the bean's local name when the request targets the pattern root.
   */
  private String resolveCommand( HttpServletRequest req ) {
    String pathInfo = req.getPathInfo();
    if ( pathInfo == null || pathInfo.isEmpty() || "/".equals( pathInfo ) ) {
      return beanId.substring( beanId.lastIndexOf( '.' ) + 1 );
    }
    return pathInfo.startsWith( "/" ) ? pathInfo.substring( 1 ) : pathInfo;
  }
}
