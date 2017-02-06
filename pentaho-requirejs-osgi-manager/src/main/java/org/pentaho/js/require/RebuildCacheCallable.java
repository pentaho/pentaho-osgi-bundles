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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class RebuildCacheCallable implements Callable<String> {
  private final String baseUrl;

  private final Map<Long, Map<String, Object>> configMap;

  // pentaho-platform-plugin configuration scripts (legacy)
  private final List<RequireJsConfiguration> requireJsConfigurations;

  public RebuildCacheCallable( String baseUrl, Map<Long, Map<String, Object>> configMap,
                               List<RequireJsConfiguration> requireJsConfigurations ) {

    // Make sure the baseUrl ends in a slash.
    // like https://github.com/requirejs/requirejs/blob/14526943c937aab3c022235335f20e260395fe15/require.js#L1145
    this.baseUrl = baseUrl.endsWith( "/" ) ? baseUrl : baseUrl + "/";

    this.configMap = configMap;
    this.requireJsConfigurations = new ArrayList<>( requireJsConfigurations );

    // sort configuration scripts by bundle ID, just to ensure some consistency
    Collections.sort( this.requireJsConfigurations, ( o1, o2 ) -> {
      long longResult = o1.getBundle().getBundleId() - o2.getBundle().getBundleId();
      if ( longResult < 0L ) {
        return -1;
      } else if ( longResult > 0L ) {
        return 1;
      } else {
        return 0;
      }
    } );

  }

  private static void makePathsAbsolute( Map<String, Object> result, String baseUrl ) {
    HashMap<String, String> paths = (HashMap<String, String>) result.get( "paths" );
    paths.forEach( ( moduleId, location ) -> {
      if ( checkNeedsBaseUrl( location ) ) {
        paths.put( moduleId, baseUrl + location );
      }
    } );

    ArrayList<Object> packages = (ArrayList<Object>) result.get( "packages" );
    final ArrayList<Object> convertedPackages = packages.stream()
        .filter( Objects::nonNull )
        .map( packageDefinition -> {
          if ( packageDefinition instanceof HashMap ) {
            final HashMap<String, String> complexPackageDefinition = (HashMap<String, String>) packageDefinition;

            if ( complexPackageDefinition.containsKey( "location" ) ) {
              String location = complexPackageDefinition.get( "location" );
              if ( checkNeedsBaseUrl( location ) ) {
                complexPackageDefinition.put( "location", baseUrl + location );
                return complexPackageDefinition;
              }
            }
          }

          return packageDefinition;
        } )
        .collect( Collectors.toCollection( ArrayList<Object>::new ) );

    result.put( "packages", convertedPackages );
  }

  private static boolean checkNeedsBaseUrl( String value ) {
    // keeping logic of https://github.com/requirejs/requirejs/blob/14526943c937aab3c022235335f20e260395fe15/require.js#L1459
    return ( value.charAt( 0 ) != '/' && !value.matches( "/^[\\w\\+\\.\\-]+:/" ) );
  }

  @Override
  public String call() throws Exception {
    RequireJsMerger merger = new RequireJsMerger();

    for ( Long bundleId : configMap.keySet() ) {
      merger.merge( configMap.get( bundleId ) );
    }

    Map<String, Object> result = merger.getRequireConfig();

    RequireJsDependencyResolver.processMetaInformation( result );

    RebuildCacheCallable.makePathsAbsolute( result, this.baseUrl );

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
