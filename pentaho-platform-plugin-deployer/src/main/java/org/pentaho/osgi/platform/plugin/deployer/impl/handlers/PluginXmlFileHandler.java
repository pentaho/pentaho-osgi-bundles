/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.osgi.platform.plugin.deployer.impl.handlers;

import org.pentaho.osgi.platform.plugin.deployer.api.XmlPluginFileHandler;

/**
 * Created by bryan on 8/29/14.
 */
public abstract class PluginXmlFileHandler extends XmlPluginFileHandler {
  public PluginXmlFileHandler( String xpath ) {
    super( xpath );
  }
  @Override public boolean handles( String fileName ) {
    if ( fileName != null && fileName.endsWith( "/plugin.xml" ) ) {
      return true;
    }
    return false;
  }
}
