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
package org.pentaho.osgi.platform.plugin.deployer.impl;

import org.pentaho.osgi.platform.plugin.deployer.api.ManifestUpdater;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;
import org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlStaticPathsHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.pentaho.osgi.platform.plugin.deployer.impl.handlers.pluginxml.PluginXmlStaticPathsHandler
    .BLUEPRINT_BEAN_NS;

/**
 * Created by bryan on 8/26/14.
 */
public class PluginMetadataImpl implements PluginMetadata {
  private final ManifestUpdater manifestUpdater = new ManifestUpdaterImpl();
  private Document blueprint;
  private final File rootDirectory;
  private List<String> contentTypes = new ArrayList<>();
  private List<Runnable> runAtEndables = new ArrayList<>(  );

  public PluginMetadataImpl( File rootDirectory ) throws ParserConfigurationException {
    blueprint = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    blueprint.appendChild( blueprint.createElementNS( PluginXmlStaticPathsHandler.BLUEPRINT_BEAN_NS, "blueprint" ) );
    this.rootDirectory = rootDirectory;
  }

  @Override public ManifestUpdater getManifestUpdater() {
    return manifestUpdater;
  }

  @Override public Document getBlueprint() {
    return blueprint;
  }

  @Override public void setBlueprint( Document blueprint ) {
    NodeList childNodes = blueprint.getDocumentElement().getChildNodes();
    for ( int i = 0; i < childNodes.getLength(); i++ ) {
      Node node = this.blueprint.importNode( childNodes.item( i ), true );
      this.blueprint.getDocumentElement().appendChild( node );
    }
  }

  @Override public void writeBlueprint( String name, OutputStream outputStream ) throws IOException {

    // Run any deferred tasks
    if ( runAtEndables.size() > 0 ) {
      ExecutorService executorService = Executors.newSingleThreadExecutor();
      runAtEndables.forEach( executorService::submit );
    }

    Result output = new StreamResult( outputStream );
    Document blueprint = getBlueprint();
    Element bean = blueprint.createElementNS( BLUEPRINT_BEAN_NS, "bean" );
    bean.setAttribute( "class", "org.pentaho.platform.pdi.BundleClassloader" );
    bean.setAttribute( "id", "classLoader" );
    Element argument = blueprint.createElementNS( BLUEPRINT_BEAN_NS,
        "argument" );
    argument.setAttribute( "ref", "blueprintBundle" );
    bean.appendChild( argument );
    argument = blueprint.createElementNS( BLUEPRINT_BEAN_NS,
        "argument" );
    argument.setAttribute( "value", manifestUpdater.getBundleSymbolicName() );
    bean.appendChild( argument );
    blueprint.getDocumentElement().appendChild( bean );

    Element service = blueprint.createElementNS( BLUEPRINT_BEAN_NS,
        "service" );
    service.setAttribute( "interface", "java.lang.ClassLoader" );
    service.setAttribute( "ref", "classLoader" );
    Element props = blueprint.createElementNS( BLUEPRINT_BEAN_NS,
        "service-properties" );
    Element entry = blueprint.createElementNS( BLUEPRINT_BEAN_NS,
        "entry" );
    entry.setAttribute( "key", "plugin-id" );
    entry.setAttribute( "value", manifestUpdater.getBundleSymbolicName() );
    props.appendChild( entry );
    service.appendChild( props );
    blueprint.getDocumentElement().appendChild( service );
    Source input = new DOMSource( blueprint );
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
      transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );
      transformer.transform( input, output );
    } catch ( TransformerException e ) {
      throw new IOException( e );
    }
  }

  @Override public FileWriter getFileWriter( String path ) throws IOException {
    File resultFile = createFile( path );
    return new FileWriter( resultFile );
  }

  private File createFile( String path ) {
    File resultFile = new File( rootDirectory.getAbsolutePath() + "/" + path );
    File parentDir = resultFile.getParentFile();
    int tries = 100;
    while ( !parentDir.exists() && tries-- > 0 ) {
      parentDir.mkdirs();
    }
    return resultFile;
  }

  @Override public OutputStream getFileOutputStream( String path ) throws IOException {
    File resultFile = createFile( path );
    return new FileOutputStream( resultFile );
  }

  @Override public void addContentType( String contentType ) {
    this.contentTypes.add( contentType );
  }

  @Override public List<String> getContentTypes() {
    return contentTypes;
  }

  @Override public void executeAtEnd( Runnable runnable ) {
    runAtEndables.add( runnable );
  }
}
