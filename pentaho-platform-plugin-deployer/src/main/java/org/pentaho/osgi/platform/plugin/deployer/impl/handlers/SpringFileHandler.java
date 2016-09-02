package org.pentaho.osgi.platform.plugin.deployer.impl.handlers;

import org.pentaho.osgi.platform.plugin.deployer.api.PluginFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginHandlingException;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import static org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlStaticPathsHandler
    .BLUEPRINT_BEAN_NS;

/**
 * Created by nbaker on 7/19/16.
 */
public class SpringFileHandler implements PluginFileHandler {

  public static final String LIB_PATTERN = ".+\\/lib\\/.+\\.jar";
  public static final String PLUGIN_SPRING_XML = ".+\\/plugin.spring.xml";
  private final Pattern beanPattern = Pattern.compile( ".*id=\"([^\\.]+\\.[^\"]+)\".+[(\\r\\n|\\r|\\n)]*" );

  @Override public boolean handles( String fileName ) {
    return fileName.matches( LIB_PATTERN ) || fileName.matches( PLUGIN_SPRING_XML );
  }

  @Override public void handle( String relativePath, File file, PluginMetadata pluginMetadata )
      throws PluginHandlingException {

    if ( relativePath.matches( LIB_PATTERN ) ) {

      FileInputStream fin = null;
      JarInputStream jarInputStream = null;
      try {
        fin = new FileInputStream( file );
        jarInputStream = new JarInputStream( fin );

        ZipEntry nextEntry;
        while ( ( nextEntry = jarInputStream.getNextEntry() ) != null ) {
          String name = nextEntry.getName();
          if ( name.matches( ".+\\.xml" ) ) {
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
      } finally {
        try {
          jarInputStream.close();
          fin.close();
        } catch ( IOException e ) {
          e.printStackTrace();
        }
      }
    } else if ( relativePath.matches( PLUGIN_SPRING_XML ) ) {
      try ( Reader fileReader = new BufferedReader( new FileReader( file ) );
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
        for ( String s : lines ) {
          Matcher matcher = beanPattern.matcher( s );
          if( matcher.matches() ) {
            String beanId = matcher.group(1);
            Document blueprint = pluginMetadata.getBlueprint();
            Element service = blueprint.createElementNS( BLUEPRINT_BEAN_NS,
                "service" );
            service.setAttribute( "interface", "javax.servlet.Servlet" );

            Element props = blueprint.createElementNS( BLUEPRINT_BEAN_NS,
                "service-properties" );
            Element entry = blueprint.createElementNS( BLUEPRINT_BEAN_NS,
                "entry" );
            String[] split = beanId.split( "\\." );
            entry.setAttribute( "key", "alias");
            entry.setAttribute( "value", "/content/analyzer/" + split[ 1 ] );
            props.appendChild( entry );

            entry = blueprint.createElementNS( BLUEPRINT_BEAN_NS,
                "entry" );
            entry.setAttribute( "key", "servlet-name");
            entry.setAttribute( "value",  beanId );
            service.appendChild( props );

            Element bean = blueprint.createElementNS( BLUEPRINT_BEAN_NS,
                "bean" );
            bean.setAttribute( "class", "org.pentaho.platform.pdi.ContentGeneratorServlet" );
            Element argument = blueprint.createElementNS( BLUEPRINT_BEAN_NS,
                "argument" );
            argument.setAttribute( "ref", "spring" );
            bean.appendChild( argument );
            argument = blueprint.createElementNS( BLUEPRINT_BEAN_NS,
                "argument" );
            argument.setAttribute( "value", beanId );
            bean.appendChild( argument );
            service.appendChild( bean );
            blueprint.getDocumentElement().appendChild( service );
          }
        }
      } catch ( IOException e ) {
        e.printStackTrace();
      }

/*
<service id="MyWhiteboardServletBeanService"
    interface="javax.servlet.Servlet">
    <service-properties>
        <entry key="alias" value="/content/analyzer/editor"/>
        <entry key="servlet-name" value="analyzerEditor"/>
    </service-properties>
    <bean class="org.pentaho.osgi.utils.ContentGeneratorServlet">
        <argument ref="spring"/>
        <argument value="xanalyzer.editor"/>
    </bean>
</service>

 */
    }

  }
}
