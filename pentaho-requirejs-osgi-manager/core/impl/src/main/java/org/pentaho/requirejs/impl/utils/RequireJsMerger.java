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
package org.pentaho.requirejs.impl.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by nantunes on 12/11/15.
 */
public class RequireJsMerger {
  private final JsonMerger merger = new JsonMerger();

  private Map<String, Object> requireConfig;

  public RequireJsMerger() {
    this.requireConfig = createEmptyRequireConfig();
  }

  public void merge( Map<String, Object> requireConfigPartial ) {
    requireConfig = merger.merge( requireConfig, requireConfigPartial );
  }

  public Map<String, Object> getRequireConfig() {
    return requireConfig;
  }

  private Map<String, Object> createEmptyRequireConfig() {
    Map<String, Object> emptyConfig = new HashMap<>();

    emptyConfig.put( "paths", new HashMap<String, Object>() );
    emptyConfig.put( "packages", new ArrayList<>() );
    emptyConfig.put( "bundles", new HashMap<String, Object>() );

    final Map<String, Object> map = new HashMap<>();
    map.put( "*", new HashMap<String, Object>() );
    emptyConfig.put( "map", map );

    emptyConfig.put( "config", new HashMap<String, Object>() );

    emptyConfig.put( "shim", new HashMap<String, Object>() );

    return emptyConfig;
  }
}
