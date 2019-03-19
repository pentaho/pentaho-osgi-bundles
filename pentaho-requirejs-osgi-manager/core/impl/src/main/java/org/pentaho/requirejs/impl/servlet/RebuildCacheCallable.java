/*!
 * Copyright 2010 - 2019 Hitachi Vantara.  All rights reserved.
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
import org.pentaho.requirejs.IPlatformPluginRequireJsConfigurations;
import org.pentaho.requirejs.IRequireJsPackageConfiguration;
import org.pentaho.requirejs.IRequireJsPackageConfigurationPlugin;
import org.pentaho.requirejs.impl.utils.JsonMerger;
import org.pentaho.requirejs.impl.utils.RequireJsDependencyResolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
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
  private final List<IRequireJsPackageConfigurationPlugin> plugins;

  private final String baseUrl;

  private final Collection<IRequireJsPackageConfiguration> packageConfigurations;

  // pentaho-platform-plugin configuration scripts
  private final List<IPlatformPluginRequireJsConfigurations> requireJsConfigurations;

  public RebuildCacheCallable( String baseUrl, Collection<IRequireJsPackageConfiguration> packageConfigurations, Collection<IPlatformPluginRequireJsConfigurations> requireJsConfigurations, List<IRequireJsPackageConfigurationPlugin> plugins ) {
    this.baseUrl = baseUrl;

    this.packageConfigurations = packageConfigurations;

    this.requireJsConfigurations = new ArrayList<>( requireJsConfigurations );

    this.plugins = plugins;
  }

  @Override
  public String call() {
    RequireJsDependencyResolver dependencyResolver = new RequireJsDependencyResolver( this.packageConfigurations );

    BiFunction<String, String, IRequireJsPackageConfiguration> getResolvedVersion = dependencyResolver::getResolvedVersion;

    Map<String, Object> requireJsConfig = createEmptyRequireConfig();

    JsonMerger merger = new JsonMerger();

    for ( IRequireJsPackageConfiguration packageConfiguration : this.packageConfigurations ) {
      packageConfiguration.processDependencies( getResolvedVersion );

      requireJsConfig = merger.merge( requireJsConfig, packageConfiguration.getRequireConfig( this.plugins ) );
    }

    RebuildCacheCallable.makePathsAbsolute( requireJsConfig, this.baseUrl );

    StringBuilder sb = new StringBuilder( JSONObject.toJSONString( requireJsConfig ) );
    sb.append( ";\n" );

    this.packageConfigurations.forEach( requireJsPackage -> {
      if ( requireJsPackage.hasScript( "preconfig" ) ) {
        final URL preconfig = requireJsPackage.getScriptResource( "preconfig" );

        if ( preconfig != null ) {
          try {
            Map<String, Object> packageInfo = new HashMap<>();
            packageInfo.put( "name", requireJsPackage.getName() );
            packageInfo.put( "version", requireJsPackage.getVersion() );
            packageInfo.put( "webRootPath", baseUrl + requireJsPackage.getWebRootPath() );

            // Additionally, `environment` is available and defined in RequireJsConfigServlet.
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

    for ( IPlatformPluginRequireJsConfigurations requireJsConfiguration : requireJsConfigurations ) {
      try {
        String bundleName = requireJsConfiguration.getName();

        sb.append( "\n\n/* Following configurations are from bundle " );
        sb.append( bundleName );
        sb.append( " */\n" );

        for ( URL configURL : requireJsConfiguration.getRequireConfigurationsURLs() ) {
          appendFromResource( sb, configURL );
        }

        sb.append( "/* End of bundle " );
        sb.append( bundleName );
        sb.append( " */\n" );
      } catch ( IOException ignored ) {
        // ignored exception
      }
    }

    return sb.toString();
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

    urlConnection = configURL.openConnection();

    try (
        InputStream inputStream = urlConnection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader( inputStream );
        BufferedReader bufferedReader = new BufferedReader( inputStreamReader ) ) {
      String input;
      while ( ( input = bufferedReader.readLine() ) != null ) {
        sb.append( input );
        sb.append( "\n" );
      }
    }
  }
}
