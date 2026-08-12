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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import static org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlStaticPathsHandler
  .BLUEPRINT_BEAN_NS;
import static org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlStaticPathsHandler.CLASS_ATTR;
import static org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlStaticPathsHandler.INTERFACE_ATTR;
import static org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlStaticPathsHandler.KEY_ATTR;
import static org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlStaticPathsHandler.REF_ATTR;
import static org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlStaticPathsHandler.VALUE_ATTR;

public class SpringFileHandler implements PluginFileHandler {

  private static final Logger log = LoggerFactory.getLogger( SpringFileHandler.class );

  public static final String PLUGIN_SPRING_XML = ".+\\/plugin.spring.xml";
  public static final String SPRING_BEANS_NS = "http://www.springframework.org/schema/beans";
  private static final String DISALLOW_DOCTYPE = "http://apache.org/xml/features/disallow-doctype-decl";
  public static final String PLUGIN_SPRING_XML_FILENAME = "plugin.spring.xml";
  /** Root of the URL space under which content generators are published, as on the Pentaho Server. */
  private static final String CONTENT_ROOT = "/content/";
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
            String contents;
            try ( ByteArrayOutputStream byteArrayOutputStream =
                    new ByteArrayOutputStream( (int) Math.min( Integer.MAX_VALUE, Math.max( 0, nextEntry.getSize() ) ) ) ) {
              byte[] buffer = new byte[ 1024 ];
              int read;
              while ( ( read = jarInputStream.read( buffer ) ) > 0 ) {
                byteArrayOutputStream.write( buffer, 0, read );
              }
              contents = byteArrayOutputStream.toString( StandardCharsets.UTF_8 );
            }
            if ( contents.contains( SPRING_BEANS_NS ) ) {
              // It is a spring file.
              try ( FileWriter fileWriter = pluginMetadata.getFileWriter( "META-INF/spring/" + name ) ) {
                fileWriter.append( contents );
              }
            }
          }
        }
      } catch ( IOException e ) {
        log.error( "Unable to extract the Spring XML files of {}", relativePath, e );
      }
    } else if ( relativePath.matches( PLUGIN_SPRING_XML ) ) {
      try ( Reader fileReader = new StringReader( new String( file, StandardCharsets.UTF_8 ) );
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

        String bundleName = pluginMetadata.getManifestUpdater().getBundleSymbolicName();
        for ( String beanId : getTopLevelBeanIds( file, relativePath ) ) {
          addContentGeneratorServlet( pluginMetadata.getBlueprint(), bundleName, beanId );
        }
      } catch ( IOException e ) {
        log.error( "Unable to process {}", relativePath, e );
      }
      return false;
    }

    return true;
  }

  /**
   * Publishes the content-generator bean {@code beanId} of the plugin's Spring context as a servlet, through a
   * {@code ContentGeneratorServlet} that resolves the bean from the plugin's {@code spring} component.
   */
  private void addContentGeneratorServlet( Document blueprint, String bundleName, String beanId ) {
    Element service = blueprint.createElementNS( BLUEPRINT_BEAN_NS, "service" );
    service.setAttribute( INTERFACE_ATTR, "javax.servlet.Servlet" );

    Element props = blueprint.createElementNS( BLUEPRINT_BEAN_NS, "service-properties" );

    // Below /content/: the bean id for the legacy geo plugin, the plugin name followed by the bean's local name when
    // the id is qualified (xanalyzer.service -> /content/analyzer/service/*), else just the plugin name.
    final String servletPattern;
    if ( "pentaho-geo".equals( bundleName ) ) {
      servletPattern = CONTENT_ROOT + beanId + "/*";
    } else if ( beanId.contains( "." ) ) {
      servletPattern = CONTENT_ROOT + bundleName + "/" + beanId.split( "\\." )[ 1 ] + "/*";
    } else {
      servletPattern = CONTENT_ROOT + bundleName + "/*";
    }

    // [PDI-20686] OSGi R7 HTTP Whiteboard properties. Pax Web 8 (Karaf 4.4.6) no longer honours
    // the legacy 'alias'/'servlet-name' pair, and emitting 'alias' alongside a whiteboard
    // pattern makes the registration ambiguous, so the endpoint answers HTTP 404.
    Element entry = blueprint.createElementNS( BLUEPRINT_BEAN_NS, "entry" );
    entry.setAttribute( KEY_ATTR, "osgi.http.whiteboard.servlet.pattern" );
    entry.setAttribute( VALUE_ATTR, servletPattern );
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

  /**
   * The ids of the top-level {@code <bean>} elements of a Spring XML file, in document order. The file is parsed
   * rather than scanned line by line, so a commented-out bean is ignored and a {@code <bean>} whose attributes span
   * several lines is still found. Nested (inner) beans are not content generators and are skipped. A file that
   * cannot be parsed - including one with a DOCTYPE, which is disallowed exactly as in {@link BlueprintFileHandler}
   * - yields no ids and is logged.
   * <p>
   * Only features are used to harden the parser, not the JAXP 1.5 {@code ACCESS_EXTERNAL_*} attributes: PDI ships
   * Xerces 2.12 in {@code lib/} and exports it to OSGi, and Xerces rejects those attributes.
   */
  private static List<String> getTopLevelBeanIds( byte[] springXml, String relativePath ) {
    List<String> ids = new ArrayList<>();
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware( true );
      factory.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
      factory.setFeature( DISALLOW_DOCTYPE, true );

      Element root = factory.newDocumentBuilder().parse( new ByteArrayInputStream( springXml ) ).getDocumentElement();
      for ( Node child = root.getFirstChild(); child != null; child = child.getNextSibling() ) {
        if ( isSpringBean( child ) ) {
          String id = ( (Element) child ).getAttribute( "id" );
          if ( !id.isEmpty() ) {
            ids.add( id );
          }
        }
      }
    } catch ( ParserConfigurationException | SAXException | IOException e ) {
      log.error( "Unable to parse {}; no content generator servlets will be published for it", relativePath, e );
    }
    return ids;
  }

  private static boolean isSpringBean( Node node ) {
    String namespace = node.getNamespaceURI();
    return node.getNodeType() == Node.ELEMENT_NODE && "bean".equals( node.getLocalName() )
      && ( namespace == null || SPRING_BEANS_NS.equals( namespace ) );
  }
}
