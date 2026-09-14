/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.osgi.platform.plugin.deployer.impl.handlers;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.osgi.platform.plugin.deployer.api.ManifestUpdater;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Covers the two [PDI-20686] changes: the OSGi R7 HTTP Whiteboard service properties, and the bean
 * pattern that must not match XML comments.
 */
public class SpringFileHandlerTest {

  private SpringFileHandler handler;
  private PluginMetadata pluginMetadata;
  private Document blueprint;
  private File scratch;

  @Before
  public void setUp() throws Exception {
    handler = new SpringFileHandler();

    blueprint = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    blueprint.appendChild( blueprint.createElement( "blueprint" ) );

    ManifestUpdater manifestUpdater = mock( ManifestUpdater.class );
    when( manifestUpdater.getBundleSymbolicName() ).thenReturn( "analyzer" );

    scratch = Files.createTempDirectory( "spring-file-handler-test" ).toFile();

    pluginMetadata = mock( PluginMetadata.class );
    when( pluginMetadata.getBlueprint() ).thenReturn( blueprint );
    when( pluginMetadata.getManifestUpdater() ).thenReturn( manifestUpdater );
    when( pluginMetadata.getFileWriter( "META-INF/spring/plugin.spring.xml" ) )
        .thenReturn( new FileWriter( new File( scratch, "plugin.spring.xml" ) ) );
  }

  private void handle( String springXml ) throws Exception {
    handler.handle( "any/plugin.spring.xml", springXml.getBytes( StandardCharsets.UTF_8 ), pluginMetadata );
  }

  /** Every {@code <service interface="javax.servlet.Servlet">} and its service-properties. */
  private List<Map<String, String>> servletServiceProperties() {
    List<Map<String, String>> all = new ArrayList<>();
    NodeList services = blueprint.getDocumentElement().getElementsByTagName( "service" );
    for ( int i = 0; i < services.getLength(); i++ ) {
      Element service = (Element) services.item( i );
      if ( !"javax.servlet.Servlet".equals( service.getAttribute( "interface" ) ) ) {
        continue;
      }
      Map<String, String> props = new HashMap<>();
      NodeList entries = service.getElementsByTagName( "entry" );
      for ( int j = 0; j < entries.getLength(); j++ ) {
        Element entry = (Element) entries.item( j );
        props.put( entry.getAttribute( "key" ), entry.getAttribute( "value" ) );
      }
      all.add( props );
    }
    return all;
  }

  @Test
  public void emitsWhiteboardPatternAndNameForEachBean() throws Exception {
    handle( "<beans>\n"
        + "  <bean id=\"xanalyzer.service\" class=\"com.pentaho.analyzer.content.AnalyzerContentGenerator\"/>\n"
        + "</beans>\n" );

    List<Map<String, String>> props = servletServiceProperties();
    assertEquals( 1, props.size() );
    assertEquals( "/content/analyzer/service/*", props.get( 0 ).get( "osgi.http.whiteboard.servlet.pattern" ) );
    assertEquals( "xanalyzer.service", props.get( 0 ).get( "osgi.http.whiteboard.servlet.name" ) );
  }

  /**
   * Pax Web 8 ignores the legacy pair, and emitting {@code alias} alongside a whiteboard pattern makes
   * the registration ambiguous, so the endpoint answers HTTP 404.
   */
  @Test
  public void doesNotEmitTheLegacyAliasOrServletNameProperties() throws Exception {
    handle( "<beans>\n"
        + "  <bean id=\"xanalyzer.service\" class=\"com.pentaho.analyzer.content.AnalyzerContentGenerator\"/>\n"
        + "</beans>\n" );

    Map<String, String> props = servletServiceProperties().get( 0 );
    assertFalse( props.containsKey( "alias" ) );
    assertFalse( props.containsKey( "servlet-name" ) );
  }

  @Test
  public void emitsOneServiceForEachBeanInTheFile() throws Exception {
    handle( "<beans>\n"
        + "  <bean id=\"xanalyzer.service\" class=\"a.A\"/>\n"
        + "  <bean id=\"xanalyzer.generatedContent\" class=\"a.A\"/>\n"
        + "  <bean id=\"xanalyzer.editor\" class=\"a.B\"/>\n"
        + "  <bean id=\"xanalyzer.backgroundExecution\" class=\"a.C\"/>\n"
        + "</beans>\n" );

    assertEquals( 4, servletServiceProperties().size() );
  }

  /**
   * The original pattern matched any line containing {@code id="..."}, so a comment produced a bogus
   * servlet registration (observed: a phantom {@code id=spring} servlet).
   */
  @Test
  public void ignoresIdAttributesInsideXmlComments() throws Exception {
    handle( "<beans>\n"
        + "  <!-- the spring context is referenced elsewhere as id=\"spring\" -->\n"
        + "  <bean id=\"xanalyzer.service\" class=\"a.A\"/>\n"
        + "</beans>\n" );

    List<Map<String, String>> props = servletServiceProperties();
    assertEquals( 1, props.size() );
    assertEquals( "xanalyzer.service", props.get( 0 ).get( "osgi.http.whiteboard.servlet.name" ) );
  }

  @Test
  public void ignoresNonBeanElementsCarryingAnId() throws Exception {
    handle( "<beans>\n"
        + "  <reference id=\"spring\" interface=\"org.springframework.context.ApplicationContext\"/>\n"
        + "  <bean id=\"xanalyzer.service\" class=\"a.A\"/>\n"
        + "</beans>\n" );

    assertEquals( 1, servletServiceProperties().size() );
  }

  @Test
  public void beanIdWithoutADotMapsToThePluginRoot() throws Exception {
    handle( "<beans>\n  <bean id=\"someBean\" class=\"a.A\"/>\n</beans>\n" );

    assertEquals( "/content/analyzer/*",
        servletServiceProperties().get( 0 ).get( "osgi.http.whiteboard.servlet.pattern" ) );
  }

  @Test
  public void wiresTheServletToTheSpringContextAndBeanId() throws Exception {
    handle( "<beans>\n  <bean id=\"xanalyzer.service\" class=\"a.A\"/>\n</beans>\n" );

    NodeList services = blueprint.getDocumentElement().getElementsByTagName( "service" );
    Element bean = (Element) ( (Element) services.item( 0 ) ).getElementsByTagName( "bean" ).item( 0 );

    assertEquals( "org.pentaho.platform.pdi.ContentGeneratorServlet", bean.getAttribute( "class" ) );

    NodeList arguments = bean.getElementsByTagName( "argument" );
    assertEquals( 2, arguments.getLength() );
    // The Spring context bean the plugin's own blueprint declares (see SpringContextFactory).
    assertEquals( "spring", ( (Element) arguments.item( 0 ) ).getAttribute( "ref" ) );
    assertEquals( "xanalyzer.service", ( (Element) arguments.item( 1 ) ).getAttribute( "value" ) );
  }

  @Test
  public void producesNoServiceForAFileWithoutBeans() throws Exception {
    handle( "<beans>\n  <!-- nothing here -->\n</beans>\n" );

    assertTrue( servletServiceProperties().isEmpty() );
  }

  @Test
  public void handlesPluginSpringXmlPath() {
    assertTrue( handler.handles( "some/dir/plugin.spring.xml" ) );
  }

  @Test
  public void copiesTheOriginalFileThrough() throws Exception {
    String xml = "<beans>\n  <bean id=\"xanalyzer.service\" class=\"a.A\"/>\n</beans>\n";
    handle( xml );

    File copied = new File( scratch, "plugin.spring.xml" );
    assertTrue( copied.exists() );
    assertEquals( xml, new String( Files.readAllBytes( copied.toPath() ), StandardCharsets.UTF_8 ) );
  }
}
