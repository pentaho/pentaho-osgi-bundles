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
package org.pentaho.osgi.i18n.resource;

import java.io.IOException;
import java.net.URL;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Created by bryan on 9/4/14.
 */
public class OSGIResourceBundle extends PropertyResourceBundle {
  private final String defaultName;

  public OSGIResourceBundle( String defaultName, URL propertyFileUrl ) throws IOException {
    this( defaultName, null, propertyFileUrl );
  }

  public OSGIResourceBundle( String defaultName, ResourceBundle parent, URL propertyFileUrl ) throws IOException {
    super( propertyFileUrl.openStream() );
    this.defaultName = defaultName;
    if ( parent != null ) {
      setParent( parent );
    }
  }

  public String getDefaultName() {
    return defaultName;
  }

  public ResourceBundle getParent() {
    return parent;
  }
}
