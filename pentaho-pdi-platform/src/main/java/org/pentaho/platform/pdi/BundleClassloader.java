/*!
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
 * Copyright (c) 2016 Pentaho Corporation..  All rights reserved.
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
