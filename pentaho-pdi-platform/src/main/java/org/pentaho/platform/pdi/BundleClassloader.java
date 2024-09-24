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
package org.pentaho.platform.pdi;

import org.ops4j.pax.web.service.spi.util.ResourceDelegatingBundleClassLoader;
import org.osgi.framework.Bundle;

import java.net.URL;
import java.util.Arrays;

/**
 * Custom ClassLoader which supports loading resources from OSGI Bundles.
 *
 * Created by nbaker on 7/28/16.
 */
public class BundleClassloader extends ResourceDelegatingBundleClassLoader {
  private final String root;

  public BundleClassloader( Bundle bundle, String name ) {
    super( Arrays.asList( bundle ) );
    this.root = name;
  }

  public String getName() {
    return root;
  }

  @Override protected URL findResource( String name ) {
    return super.findResource( root + "/" + name );
  }

}
