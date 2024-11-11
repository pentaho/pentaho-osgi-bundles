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

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

/**
 * Finds blueprint files embedded one directory down and moves it up where it can be picked up by OSGI
 * Created by nbaker on 7/21/16.
 */
public class BlueprintFileHandler implements PluginFileHandler {

  public static final String JAR = ".jar";
  public static final String XML = ".xml";
  public static final String OSGI_INF_BLUEPRINT = "OSGI-INF/blueprint/";

  @Override public boolean handles( String fileName ) {
    return fileName != null && fileName.contains( OSGI_INF_BLUEPRINT ) && fileName.endsWith( XML );
  }

  @Override public boolean handle( String relativePath, byte[] file, PluginMetadata pluginMetadata )
      throws PluginHandlingException {
    try {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
      documentBuilderFactory.setFeature( "http://apache.org/xml/features/disallow-doctype-decl", true );
      documentBuilderFactory.setNamespaceAware( true );
      Document blueprint =
        documentBuilderFactory.newDocumentBuilder().parse( new ByteArrayInputStream( file ) );
      pluginMetadata.setBlueprint( blueprint );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    return false;
  }
}

