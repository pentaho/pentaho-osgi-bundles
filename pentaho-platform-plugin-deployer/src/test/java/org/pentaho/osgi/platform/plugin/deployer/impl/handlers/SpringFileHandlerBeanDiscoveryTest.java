package org.pentaho.osgi.platform.plugin.deployer.impl.handlers;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.pentaho.osgi.platform.plugin.deployer.api.ManifestUpdater;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Bean discovery in {@code plugin.spring.xml}: each file declares exactly one real content-generator bean,
 * {@code xanalyzer.service}, next to something that must not be published as a servlet.
 */
@RunWith( Parameterized.class )
public class SpringFileHandlerBeanDiscoveryTest {

  private static final String BEAN = "  <bean id=\"xanalyzer.service\" class=\"a.A\"/>\n";

  @Parameters( name = "{0}" )
  public static Collection<Object[]> springFiles() {
    return Arrays.asList( new Object[][] {
      // The original line regex matched any line containing id="...", so a comment produced a phantom servlet.
      { "an id attribute inside an XML comment",
        "<beans>\n  <!-- the spring context is referenced elsewhere as id=\"spring\" -->\n" + BEAN + "</beans>\n" },
      { "a non-bean element carrying an id",
        "<beans>\n  <reference id=\"spring\" interface=\"org.springframework.context.ApplicationContext\"/>\n"
          + BEAN + "</beans>\n" },
      { "a whole bean commented out on a single line",
        "<beans>\n  <!-- <bean id=\"spring\" class=\"Example\"/> -->\n" + BEAN + "</beans>\n" },
      { "a bean whose attributes span several lines",
        "<beans>\n  <bean\n      class=\"a.A\"\n      id=\"xanalyzer.service\"\n      scope=\"prototype\"/>\n</beans>\n" },
      // Inner beans are wiring details of another bean, not content generators.
      { "a nested (inner) bean",
        "<beans>\n  <bean id=\"xanalyzer.service\" class=\"a.A\">\n"
          + "    <property name=\"helper\"><bean id=\"inner\" class=\"a.B\"/></property>\n  </bean>\n</beans>\n" },
      // Only Spring's own <bean> counts - not a same-named element from another namespace.
      { "a bean element from another namespace",
        "<beans xmlns=\"http://www.springframework.org/schema/beans\" xmlns:other=\"urn:other\">\n"
          + "  <other:bean id=\"notASpringBean\"/>\n" + BEAN + "</beans>\n" },
    } );
  }

  @Parameter( 0 )
  public String description;

  @Parameter( 1 )
  public String springXml;

  @Rule
  public TemporaryFolder scratch = new TemporaryFolder();

  private PluginMetadata pluginMetadata;
  private Document blueprint;

  @Before
  public void setUp() throws Exception {
    blueprint = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    blueprint.appendChild( blueprint.createElement( "blueprint" ) );

    ManifestUpdater manifestUpdater = mock( ManifestUpdater.class );
    when( manifestUpdater.getBundleSymbolicName() ).thenReturn( "analyzer" );

    pluginMetadata = mock( PluginMetadata.class );
    when( pluginMetadata.getBlueprint() ).thenReturn( blueprint );
    when( pluginMetadata.getManifestUpdater() ).thenReturn( manifestUpdater );
    when( pluginMetadata.getFileWriter( "META-INF/spring/plugin.spring.xml" ) )
      .thenReturn( new FileWriter( new File( scratch.getRoot(), "plugin.spring.xml" ) ) );
  }

  @Test
  public void publishesOnlyTheRealContentGeneratorBean() throws Exception {
    new SpringFileHandler()
      .handle( "any/plugin.spring.xml", springXml.getBytes( StandardCharsets.UTF_8 ), pluginMetadata );

    assertEquals( description, List.of( "xanalyzer.service" ), publishedServletNames() );
  }

  private List<String> publishedServletNames() {
    List<String> names = new ArrayList<>();
    NodeList entries = blueprint.getDocumentElement().getElementsByTagName( "entry" );
    for ( int i = 0; i < entries.getLength(); i++ ) {
      Element entry = (Element) entries.item( i );
      if ( "osgi.http.whiteboard.servlet.name".equals( entry.getAttribute( "key" ) ) ) {
        names.add( entry.getAttribute( "value" ) );
      }
    }
    return names;
  }
}
