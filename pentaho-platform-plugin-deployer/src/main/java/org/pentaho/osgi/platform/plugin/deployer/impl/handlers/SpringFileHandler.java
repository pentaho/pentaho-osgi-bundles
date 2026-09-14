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

package org.pentaho.osgi.platform.plugin.deployer.impl.handlers;

import org.pentaho.osgi.platform.plugin.deployer.api.PluginFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginHandlingException;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import static org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlStaticPathsHandler
  .BLUEPRINT_BEAN_NS;
import static org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlStaticPathsHandler.CLASS_ATTR;
import static org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlStaticPathsHandler.INTERFACE_ATTR;
import static org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlStaticPathsHandler.KEY_ATTR;
import static org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlStaticPathsHandler.REF_ATTR;
import static org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlStaticPathsHandler.VALUE_ATTR;

/**
 * Created by nbaker on 7/19/16.
 */
public class SpringFileHandler implements PluginFileHandler {

  public static final String PLUGIN_SPRING_XML = ".+\\/plugin.spring.xml";
  /**
   * Matches a single-line Spring {@code <bean id="..."/>} declaration. The {@code <bean} prefix is
   * required so that comments or other elements that happen to carry an {@code id="..."} attribute do
   * not produce spurious servlet registrations.
   * <p>
   * The leading/trailing {@code .*} and the greedy {@code [^>]*} of the original pattern, combined
   * with the {@code .+?} capture group, were vulnerable to catastrophic backtracking (exponential
   * runtime) on crafted input, e.g. a long line with an unterminated {@code id="} attribute. The
   * pattern below is used with {@link Matcher#find()} instead of {@code matches()} - so it no longer
   * needs the wrapping {@code .*} - and uses a reluctant {@code [^>]*?} followed by a literal
   * {@code id="}, so there is only one unbounded quantifier to satisfy instead of two competing over
   * the same characters.
   */
  private final Pattern beanPattern = Pattern.compile( "<bean\\s[^>]*?id=\"([^\"]*)\"" );
  public static final String PLUGIN_SPRING_XML_FILENAME = "plugin.spring.xml";
  public static final String LIB = "/lib/";
  public static final String JAR = ".jar";
  public static final String XML = ".xml";

  @Override public boolean handles( String fileName ) {
    return fileName != null
            && ( ( fileName.contains( LIB ) && fileName.endsWith( JAR ) ) || fileName.endsWith( PLUGIN_SPRING_XML_FILENAME ) );
  }

  @Override public boolean handle( String relativePath, byte[] file, PluginMetadata pluginMetadata )
    throws PluginHandlingException {

    if ( relativePath.contains( LIB ) && relativePath.endsWith( JAR ) ) {


      try ( ByteArrayInputStream fin = new ByteArrayInputStream( file );
          JarInputStream jarInputStream = new JarInputStream( fin ); ) {

        ZipEntry nextEntry;
        while ( ( nextEntry = jarInputStream.getNextEntry() ) != null ) {
          String name = nextEntry.getName();
          if ( name.endsWith( XML ) ) {
            // have to crack it open unfortunately.
            //
            ByteArrayOutputStream byteArrayOutputStream = null;
            String contents;
            try {
              byteArrayOutputStream =
                new ByteArrayOutputStream( (int) Math.min( Integer.MAX_VALUE, Math.max( 0, nextEntry.getSize() ) ) );
              byte[] buffer = new byte[ 1024 ];
              int read;
              while ( ( read = jarInputStream.read( buffer ) ) > 0 ) {
                byteArrayOutputStream.write( buffer, 0, read );
              }
              contents = byteArrayOutputStream.toString( "UTF-8" );
            } finally {
              byteArrayOutputStream.close();
            }
            if ( contents.contains( "http://www.springframework.org/schema/beans" ) ) {
              // It is a spring file.
              FileWriter fileWriter = pluginMetadata.getFileWriter( "META-INF/spring/" + name );
              fileWriter.append( contents );
              fileWriter.close();
            }
          }
        }
      } catch ( IOException e ) {
        e.printStackTrace();
      }
    } else if ( relativePath.matches( PLUGIN_SPRING_XML ) ) {
      try ( Reader fileReader = new StringReader( new String( file, "UTF-8"  ) );
            FileWriter fileWriter = pluginMetadata.getFileWriter( "META-INF/spring/plugin.spring.xml" ) ) {
        fileReader.mark( 0 );
        int read;
        StringWriter stringWriter = new StringWriter( 4098 );
        while ( ( read = fileReader.read() ) != -1 ) {
          stringWriter.write( read );
        }
        String contents = stringWriter.toString();
        // copy out to the new location as-is
        fileWriter.write( contents );

        String[] lines = contents.split( "\\n" );

        //        lines.stream().filter(
        //            s -> s.matches( "\".+\\..+\"" )
        //        ).forEach( s -> {
        String bundleName = pluginMetadata.getManifestUpdater().getBundleSymbolicName();
        for ( String s : lines ) {
          Matcher matcher = beanPattern.matcher( s );
          if ( matcher.find() ) {
            String beanId = matcher.group( 1 );
            Document blueprint = pluginMetadata.getBlueprint();

            Element service = blueprint.createElementNS( BLUEPRINT_BEAN_NS, "service" );
            service.setAttribute( INTERFACE_ATTR, "javax.servlet.Servlet" );

            Element props = blueprint.createElementNS( BLUEPRINT_BEAN_NS, "service-properties" );

            String value = "/content/" + bundleName;
            if ( "pentaho-geo".equals( bundleName ) ) {
              value = "/content/" + beanId;
            } else if ( beanId.contains( "." ) ) {
              String[] split = beanId.split( "\\." );
              value = "/content/" + bundleName + "/" + split[ 1 ];
            }

            // [PDI-20686] OSGi R7 HTTP Whiteboard properties. Pax Web 8 (Karaf 4.4.6) no longer honours
            // the legacy 'alias'/'servlet-name' pair, and emitting 'alias' alongside a whiteboard
            // pattern makes the registration ambiguous, so the endpoint answers HTTP 404.
            Element entry = blueprint.createElementNS( BLUEPRINT_BEAN_NS, "entry" );
            entry.setAttribute( KEY_ATTR, "osgi.http.whiteboard.servlet.pattern" );
            entry.setAttribute( VALUE_ATTR, value + "/*" );
            props.appendChild( entry );

            entry = blueprint.createElementNS( BLUEPRINT_BEAN_NS, "entry" );
            entry.setAttribute( KEY_ATTR, "osgi.http.whiteboard.servlet.name" );
            entry.setAttribute( VALUE_ATTR, beanId );
            props.appendChild( entry );

            service.appendChild( props );

            Element bean = blueprint.createElementNS( BLUEPRINT_BEAN_NS, "bean" );
            bean.setAttribute( CLASS_ATTR, "org.pentaho.platform.pdi.ContentGeneratorServlet" );
            Element argument = blueprint.createElementNS( BLUEPRINT_BEAN_NS, "argument" );
            argument.setAttribute( REF_ATTR, "spring" );
            bean.appendChild( argument );
            argument = blueprint.createElementNS( BLUEPRINT_BEAN_NS, "argument" );
            argument.setAttribute( VALUE_ATTR, beanId );
            bean.appendChild( argument );
            service.appendChild( bean );
            blueprint.getDocumentElement().appendChild( service );
          }
        }
      } catch ( IOException e ) {
        e.printStackTrace();
      }
      return false;
    }

    return true;
  }
}
