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
import java.util.ResourceBundle;

/**
 * Created by bryan on 9/5/14.
 */
public class OSGIResourceBundleFactory {
  private final int priority;
  private final String defaultName;
  private final String relativeName;
  private final URL propertyFileUrl;
  private ResourceBundle previousParent = null;
  private OSGIResourceBundle previousResult = null;

  public OSGIResourceBundleFactory( String defaultName, String relativeName, URL propertyFileUrl, int priority ) {
    this.defaultName = defaultName;
    this.priority = priority;
    this.relativeName = relativeName;
    this.propertyFileUrl = propertyFileUrl;
  }

  public synchronized OSGIResourceBundle getBundle( ResourceBundle parent ) throws IOException {
    if ( previousResult == null || previousParent != parent ) {
      previousParent = parent;
      previousResult = new OSGIResourceBundle( defaultName, parent, propertyFileUrl );
    }
    return previousResult;
  }

  public int getPriority() {
    return priority;
  }

  public String getPropertyFilePath() {
    return relativeName;
  }
}
