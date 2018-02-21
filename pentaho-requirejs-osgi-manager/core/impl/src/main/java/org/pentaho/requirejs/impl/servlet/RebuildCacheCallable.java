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
package org.pentaho.requirejs.impl.servlet;

import org.json.simple.JSONObject;
import org.osgi.framework.Bundle;
import org.pentaho.requirejs.impl.types.RequireJsConfiguration;
import org.pentaho.requirejs.impl.utils.RequireJsDependencyResolver;
import org.pentaho.requirejs.impl.utils.RequireJsMerger;
import org.pentaho.requirejs.impl.plugins.AmdPluginConfig;
import org.pentaho.requirejs.impl.plugins.NomAmdPackageShim;
import org.pentaho.requirejs.impl.plugins.TypeAndInstanceInfoPluginConfig;
import org.pentaho.requirejs.RequireJsPackageConfiguration;
import org.pentaho.requirejs.RequireJsPackageConfigurationPlugin;

import java.io.BufferedReader;
import java.io.IOException;
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
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class RebuildCacheCallable implements Callable<String> {
  /**
   * Plugins that can customize each package's requirejs configuration.
   */
  private final List<RequireJsPackageConfigurationPlugin> plugins;

  private final String baseUrl;

  private final List<RequireJsPackageConfiguration> packageConfigurations;

  // pentaho-platform-plugin configuration scripts (legacy)
  private final List<RequireJsConfiguration> requireJsConfigurations;

  public RebuildCacheCallable( String baseUrl, List<RequireJsPackageConfiguration> packageConfigurations, List<RequireJsConfiguration> requireJsConfigurations ) {
    this.baseUrl = baseUrl;

    this.packageConfigurations = packageConfigurations;

    this.requireJsConfigurations = requireJsConfigurations;

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

    // TODO: This shouldn't be a fixed list, but instead services registered by any bundle and injected here
    this.plugins = new ArrayList<>( 3 );
    // TODO: This belongs in a Type API webpackage bundle
    this.plugins.add( new TypeAndInstanceInfoPluginConfig() );
    // TODO: This belongs in a Core Utils webpackage bundle
    this.plugins.add( new AmdPluginConfig() );
    // TODO: This belongs in a pentaho-webjars-deployer's bundle
    this.plugins.add( new NomAmdPackageShim() );
  }

  @Override
  public String call() {
    RequireJsDependencyResolver dependencyResolver = RequireJsDependencyResolver.createDependencyResolver( this.packageConfigurations );

    BiFunction<String, String, RequireJsPackageConfiguration> getResolvedVersion = dependencyResolver::getResolvedVersion;

    RequireJsMerger merger = new RequireJsMerger();
    this.packageConfigurations.forEach( config -> {
      config.processDependencies( getResolvedVersion );
      merger.merge( config.getRequireConfig( this.plugins ) );
    } );

    Map<String, Object> result = merger.getRequireConfig();

    RebuildCacheCallable.makePathsAbsolute( result, this.baseUrl );

    StringBuilder sb = new StringBuilder( JSONObject.toJSONString( result ) );
    sb.append( ";\n" );

    this.packageConfigurations.forEach( requireJsPackage -> {
      if ( requireJsPackage.hasScript( "preconfig" ) ) {
        final URL preconfig = requireJsPackage.getScriptResource( "preconfig" );

        if ( preconfig != null ) {
          try {
            Map<String, Object> packageInfo = new HashMap<>();
            packageInfo.put( "name", requireJsPackage.getName() );
            packageInfo.put( "version", requireJsPackage.getVersion() );
            packageInfo.put( "versionedName", requireJsPackage.getVersionedName() );
            packageInfo.put( "webRootPath", baseUrl + requireJsPackage.getWebRootPath() );

            sb.append( "\n\n(function(requireCfg, packageInfo, getVersionedModuleId) {\n" );
            sb.append( "  try {\n" );
            appendFromResource( sb, preconfig );
            sb.append( "  } catch(e) {\n" );
            sb.append( "    console.error(\"Failed executing " + requireJsPackage.getName() + "@" + requireJsPackage.getVersion() + " preconfig script\");\n" );
            sb.append( "    console.error(e.stack);\n" );
            sb.append( "  }\n" );
            sb.append( "\n})(requireCfg, " + JSONObject.toJSONString( packageInfo ) + ", getVersionedModuleId.bind(null, " + JSONObject.toJSONString( requireJsPackage.getBaseModuleIdsMapping() ) + "));\n" );
          } catch ( IOException ignored ) {
            // ignored exception
          }
        }
      }
    } );

    for ( RequireJsConfiguration requireJsConfiguration : requireJsConfigurations ) {
      try {
        sb.append( "\n\n/* Following configurations are from bundle " );
        Bundle bundle = requireJsConfiguration.getBundle();
        String bundleName = "[" + bundle.getBundleId() + "] - " + bundle.getSymbolicName() + ":" + bundle.getVersion();
        sb.append( bundleName );
        sb.append( "*/\n" );

        for ( String config : requireJsConfiguration.getRequireConfigurations() ) {
          appendFromResource( sb, bundle.getResource( config ) );
        }

        sb.append( "/* End of bundle " );
        sb.append( bundleName );
        sb.append( "*/\n" );
      } catch ( IOException ignored ) {
        // ignored exception
      }
    }

    return sb.toString();
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
        .collect( Collectors.toCollection( ArrayList::new ) );

    result.put( "packages", convertedPackages );
  }

  private static boolean checkNeedsBaseUrl( String value ) {
    // keeping logic of https://github.com/requirejs/requirejs/blob/14526943c937aab3c022235335f20e260395fe15/require.js#L1459
    return ( value.charAt( 0 ) != '/' && !value.matches( "/^[\\w\\+\\.\\-]+:/" ) );
  }

  private void appendFromResource( StringBuilder sb, URL configURL ) throws IOException {
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
}
