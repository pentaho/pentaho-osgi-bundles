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

package org.pentaho.js.require;

import org.json.simple.JSONObject;
import org.osgi.framework.Bundle;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by bryan on 8/15/14.
 */
public class RebuildCacheCallable implements Callable<String> {
  private final Map<Long, Map<String, Object>> configMap;
  private final List<RequireJsConfiguration> requireJsConfigurations;

  public RebuildCacheCallable( Map<Long, Map<String, Object>> configMap,
                               List<RequireJsConfiguration> requireJsConfigurations ) {
    this.configMap = configMap;
    this.requireJsConfigurations = new ArrayList<>( requireJsConfigurations );
    Collections.sort( this.requireJsConfigurations, new Comparator<RequireJsConfiguration>() {
      @Override public int compare( RequireJsConfiguration o1, RequireJsConfiguration o2 ) {
        long longResult = o1.getBundle().getBundleId() - o2.getBundle().getBundleId();
        if ( longResult < 0L ) {
          return -1;
        } else if ( longResult > 0L ) {
          return 1;
        } else {
          return 0;
        }
      }
    } );
  }

  @Override
  public String call() throws Exception {
    RequireJsMerger merger = new RequireJsMerger();

    for ( Long bundleId : configMap.keySet() ) {
      merger.merge( configMap.get( bundleId ) );
    }

    Map<String, Object> result = merger.getRequireConfig();

    RequireJsDependencyResolver.processMetaInformation( result );

    StringBuilder sb = new StringBuilder( JSONObject.toJSONString( result ) );
    sb.append( ";" );

    for ( RequireJsConfiguration requireJsConfiguration : requireJsConfigurations ) {
      sb.append( "\n\n/* Following configurations are from bundle " );
      Bundle bundle = requireJsConfiguration.getBundle();
      String bundleName = "[" + bundle.getBundleId() + "] - " + bundle.getSymbolicName() + ":" + bundle.getVersion();
      sb.append( bundleName );
      sb.append( "*/\n" );

      for ( String config : requireJsConfiguration.getRequireConfigurations() ) {
        URL configURL = bundle.getResource( config );
        URLConnection urlConnection;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

        try {
          urlConnection = configURL.openConnection();
          inputStream = urlConnection.getInputStream();
          inputStreamReader = new InputStreamReader( inputStream );
          bufferedReader = new BufferedReader( inputStreamReader );

          String input;
          while ( ( input = bufferedReader.readLine() ) != null ) {
            sb.append( input );
            sb.append( "\n" );
          }
        } finally {
          if ( bufferedReader != null ) {
            bufferedReader.close();
          }
          if ( inputStreamReader != null ) {
            inputStreamReader.close();
          }
          if ( inputStream != null ) {
            inputStream.close();
          }
        }
      }

      sb.append( "/* End of bundle " );
      sb.append( bundleName );
      sb.append( "*/\n" );
    }

    return sb.toString();
  }
}
