/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2017 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */

package org.pentaho.osgi.platform.plugin.deployer.impl.handlers;

import org.pentaho.osgi.platform.plugin.deployer.api.PluginFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginHandlingException;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

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

