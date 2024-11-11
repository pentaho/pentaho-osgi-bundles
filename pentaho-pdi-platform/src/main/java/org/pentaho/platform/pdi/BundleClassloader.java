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
