/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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

/**
 * Created by nbaker on 7/19/16.
 */
public class SpringFileHandler implements PluginFileHandler {

  public static final String PLUGIN_SPRING_XML = ".+\\/plugin.spring.xml";
  private final Pattern beanPattern = Pattern.compile( ".*id=\"(.+?)\".+[(\\r\\n|\\r|\\n)]*" );
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
          if ( matcher.matches() ) {
            String beanId = matcher.group( 1 );
            Document blueprint = pluginMetadata.getBlueprint();
            Element service = blueprint.createElementNS( BLUEPRINT_BEAN_NS, "service" );
            service.setAttribute( "interface", "javax.servlet.Servlet" );

            Element props = blueprint.createElementNS( BLUEPRINT_BEAN_NS, "service-properties" );
            Element entry = blueprint.createElementNS( BLUEPRINT_BEAN_NS, "entry" );

            String value = "/content/" + bundleName;
            if ( "pentaho-geo".equals( bundleName ) ) {
              value = "/content/" + beanId;
            } else if ( beanId.contains( "." ) ) {
              String[] split = beanId.split( "\\." );
              value = "/content/" + bundleName + "/" + split[ 1 ];
            }
            entry.setAttribute( "key", "alias" );
            entry.setAttribute( "value", value );

            props.appendChild( entry );

            entry = blueprint.createElementNS( BLUEPRINT_BEAN_NS, "entry" );
            entry.setAttribute( "key", "servlet-name" );
            entry.setAttribute( "value", beanId );
            service.appendChild( props );

            Element bean = blueprint.createElementNS( BLUEPRINT_BEAN_NS, "bean" );
            bean.setAttribute( "class", "org.pentaho.platform.pdi.ContentGeneratorServlet" );
            Element argument = blueprint.createElementNS( BLUEPRINT_BEAN_NS, "argument" );
            argument.setAttribute( "ref", "spring" );
            bean.appendChild( argument );
            argument = blueprint.createElementNS( BLUEPRINT_BEAN_NS, "argument" );
            argument.setAttribute( "value", beanId );
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
