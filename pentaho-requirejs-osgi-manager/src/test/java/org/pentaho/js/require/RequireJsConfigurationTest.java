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

package org.pentaho.js.require;

import org.junit.Test;
import org.osgi.framework.Bundle;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Created by bryan on 9/2/14.
 */
public class RequireJsConfigurationTest {
  @Test
  public void testConstructor() {
    Bundle bundle = mock( Bundle.class );
    List<String> configs = new ArrayList<String>();
    configs.add( "test" );
    RequireJsConfiguration configuration = new RequireJsConfiguration( bundle, configs );
    assertEquals( bundle, configuration.getBundle() );
    assertEquals( configs, configuration.getRequireConfigurations() );
  }
}
