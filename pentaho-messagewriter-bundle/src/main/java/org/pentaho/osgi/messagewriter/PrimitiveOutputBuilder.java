/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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

package org.pentaho.osgi.messagewriter;

import org.pentaho.osgi.messagewriter.builder.OutputBuilder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by bryan on 9/11/14.
 */
public interface PrimitiveOutputBuilder extends OutputBuilder {
  public static final Set<Class<?>> primitiveClasses =
    new HashSet<Class<?>>( Arrays.asList( Double.class, Integer.class, Long.class, Boolean.class, String.class ) );

  public void set( Object value ) throws OutputException;
}
