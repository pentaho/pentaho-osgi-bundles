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

/**
 * Created by nbaker on 7/25/16.
 */

import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.web.http.api.resources.ContentGeneratorDescriptor;
import org.pentaho.platform.web.http.api.resources.GeneratorStreamingOutput;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class ContentGeneratorServlet extends HttpServlet {

  /**
   * 
   */
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
    Thread.currentThread().setContextClassLoader( applicationContext.getClassLoader() );

    GeneratorStreamingOutput generatorStreamingOutput = new GeneratorStreamingOutput( contentGenerator,
        new ContentGeneratorDescriptor() {
          @Override public String getContentGeneratorId() {
            return null;
          }

          @Override public String getServicingFileType() {
            return null;
          }

          @Override public String getPluginId() {
            String requestURI = req.getRequestURI();
            if ( requestURI.contains( "/content" ) ) {
              return requestURI.substring( requestURI.indexOf( "/", 1 ) );
            }
            return null;
          }
        },
        req,
        resp,
        Collections.emptyList(),
        null,
        req.getPathInfo() != null ? req.getPathInfo().substring( 1 ) : beanId.substring( beanId.lastIndexOf( "." ) + 1 )
    );

    generatorStreamingOutput.write( resp.getOutputStream(), null );
  }

  @Override protected void doGet( HttpServletRequest req, final HttpServletResponse resp )
      throws ServletException, IOException {
    service( req, resp );
  }

  @Override protected void doPost( HttpServletRequest req, HttpServletResponse resp )
      throws ServletException, IOException {
    service( req, resp );
  }
}
