/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.osgi.platform.plugin.deployer.impl.handlers;

import org.pentaho.osgi.platform.plugin.deployer.api.XmlPluginFileHandler;

/**
 * Created by bryan on 8/29/14.
 */
public abstract class PluginXmlFileHandler extends XmlPluginFileHandler {

  public static final String PLUGIN_XML_FILENAME = "plugin.xml";

  public PluginXmlFileHandler( String xpath ) {
    super( xpath );
  }
  @Override public boolean handles( String fileName ) {
    return fileName != null && fileName.endsWith( "/" + PLUGIN_XML_FILENAME );
  }
}
