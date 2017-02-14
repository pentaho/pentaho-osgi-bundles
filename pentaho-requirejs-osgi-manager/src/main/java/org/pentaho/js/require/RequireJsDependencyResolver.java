/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2015-2017 by Pentaho : http://www.pentaho.com
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

import com.github.zafarkhaja.semver.Version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RequireJsDependencyResolver {
  public static void processMetaInformation( Map<String, Object> result ) {
    if ( !result.containsKey( "requirejs-osgi-meta" ) ) {
      return;
    }

    RequireJsDependencyResolver resolver = new RequireJsDependencyResolver( result );
    resolver.run();

    result.remove( "requirejs-osgi-meta" );
  }

  private Map<String, Object> requireConfig;

  final HashMap<String, ModuleRequirements> requirements;
  HashMap<String, HashMap<String, HashMap<String, ?>>> availableModules;

  private RequireJsDependencyResolver( Map<String, Object> requireConfig ) {
    this.requireConfig = requireConfig;

    requirements = new HashMap<>();
  }

  private void run() {
    final HashMap<String, Object> meta = (HashMap<String, Object>) requireConfig.get( "requirejs-osgi-meta" );

    if ( meta.containsKey( "modules" ) ) {
      availableModules = (HashMap<String, HashMap<String, HashMap<String, ?>>>) meta.get( "modules" );
    } else {
      availableModules = new HashMap<>();
    }

    HashMap<String, HashMap<String, HashMap<String, String>>> artifacts;
    if ( meta.containsKey( "artifacts" ) ) {
      artifacts = (HashMap<String, HashMap<String, HashMap<String, String>>>) meta.get( "artifacts" );
    } else {
      artifacts = new HashMap<>();
    }

    for ( String module : availableModules.keySet() ) {
      final HashMap<String, HashMap<String, ?>> moduleInfo = availableModules.get( module );

      for ( String version : moduleInfo.keySet() ) {
        final HashMap<String, HashMap<String, String>> versionInfo = (HashMap<String, HashMap<String, String>>) moduleInfo.get( version );

        if ( versionInfo.containsKey( "dependencies" ) ) {
          final HashMap<String, String> dependencies = versionInfo.get( "dependencies" );

          final HashMap<String, String> processedDependencies = new HashMap<>();

          for ( String dependencyId : dependencies.keySet() ) {
            String versionRequirement = dependencies.get( dependencyId );

            if ( dependencyId.startsWith( "mvn:" ) ) {
              final String dependencyArtifact = dependencyId.substring( 4 );

              if ( artifacts.containsKey( dependencyArtifact ) ) {
                HashMap<String, HashMap<String, String>> artifactInfo = artifacts.get( dependencyArtifact );

                final ArrayList<String> resolvedArtifacts = resolveVersion( versionRequirement, artifactInfo.keySet() );

                for ( String artifactVersion : resolvedArtifacts ) {
                  HashMap<String, String> dependencyModules = artifactInfo.get( artifactVersion );

                  for ( String dependencyModuleId : dependencyModules.keySet() ) {
                    String dependencyVersionRequirement = dependencyModules.get( dependencyModuleId );
                    if ( processedDependencies.containsKey( dependencyModuleId ) ) {
                      dependencyVersionRequirement = dependencyVersionRequirement + " | " + processedDependencies.get( dependencyModuleId );
                    }

                    processModuleRequirement( dependencyModuleId, dependencyVersionRequirement );
                    processedDependencies.put( dependencyModuleId, dependencyVersionRequirement );
                  }
                }
              }
            } else {
              processModuleRequirement( dependencyId, versionRequirement );
              processedDependencies.put( dependencyId, versionRequirement );
            }
          }

          versionInfo.put( "processed", processedDependencies );
        }
      }
    }

    for ( String moduleId : requirements.keySet() ) {
      ModuleRequirements moduleRequirement = requirements.get( moduleId );
      moduleRequirement.resolve();
    }

    Map<String, HashMap<String, ?>> map;
    if ( !requireConfig.containsKey( "map" ) ) {
      map = new HashMap<>();
    } else {
      map = (Map<String, HashMap<String, ?>>) requireConfig.get( "map" );
    }

    for ( String module : availableModules.keySet() ) {
      final HashMap<String, HashMap<String, ?>> moduleInfo = availableModules.get( module );

      for ( String version : moduleInfo.keySet() ) {
        final HashMap<String, Object> versionInfo = (HashMap<String, Object>) moduleInfo.get( version );

        final boolean isAmdModule =
            versionInfo.containsKey( "isAmdPackage" ) && ( (Boolean) versionInfo.get( "isAmdPackage" ) ).booleanValue();
        String exports = null;
        if ( !isAmdModule && versionInfo.containsKey( "exports" ) ) {
          exports = (String) versionInfo.get( "exports" );
        }

        Set<String> moduleShimDependencies = new LinkedHashSet<>();

        if ( versionInfo.containsKey( "processed" ) ) {
          final HashMap<String, String> processedDependencies = (HashMap<String, String>) versionInfo.get( "processed" );

          final HashMap<String, String> resolved = new HashMap<>();

          HashMap<String, String> moduleMap;
          if ( !map.containsKey( module + "_" + version ) ) {
            moduleMap = new HashMap<>();
          } else {
            moduleMap = (HashMap<String, String>) map.get( module + "_" + version );
          }

          for ( String dependencyModuleId : processedDependencies.keySet() ) {
            String dependencyModuleVersion = processedDependencies.get( dependencyModuleId );
            final String dependencyResolvedVersion = requirements.get( dependencyModuleId ).getResolution( dependencyModuleVersion );

            if ( dependencyResolvedVersion != null ) {
              resolved.put( dependencyModuleId, dependencyResolvedVersion );

              moduleMap.put( dependencyModuleId, dependencyModuleId + "_" + dependencyResolvedVersion );

              if ( !isAmdModule ) {
                final HashMap<String, ?> dependencyInfo =
                    availableModules.get( dependencyModuleId ).get( dependencyResolvedVersion );
                final boolean dependencyIsAmdModule =
                    dependencyInfo.containsKey( "isAmdPackage" ) && ( (Boolean) dependencyInfo.get( "isAmdPackage" ) )
                        .booleanValue();

                if ( !dependencyIsAmdModule ) {
                  moduleShimDependencies.add( dependencyModuleId + "_" + dependencyResolvedVersion );
                }
              }
            }
          }

          if ( !resolved.isEmpty() ) {
            versionInfo.put( "resolved", resolved );

            map.put( module + "_" + version, moduleMap );
          }
        }

        if ( !isAmdModule ) {
          Map<String, Object> shim;
          if ( !requireConfig.containsKey( "shim" ) ) {
            shim = new HashMap<>();
          } else {
            shim = (HashMap<String, Object>) requireConfig.get( "shim" );
          }

          Map<String, Object> moduleShim = new HashMap<>();
          if ( shim.containsKey( module + "_" + version ) ) {
            Object originalShim = shim.get( module + "_" + version );

            if ( originalShim instanceof List ) {
              moduleShimDependencies.addAll( (List<String>) originalShim );
            } else if ( originalShim instanceof Map ) {
              if ( ( (Map) originalShim ).containsKey( "deps" ) ) {
                moduleShimDependencies.addAll( (List<String>) ( (Map) originalShim ).get( "deps" ) );
              }
            }
          }

          if ( !moduleShimDependencies.isEmpty() ) {
            moduleShim.put( "deps", new ArrayList<>( moduleShimDependencies ) );
          }

          if ( exports != null && !moduleShim.containsKey( "exports" ) ) {
            moduleShim.put( "exports", exports );
          }

          shim.put( module + "_" + version, moduleShim );

          List<Object> packages = (List<Object>) versionInfo.get( "packages" );
          if ( packages != null ) {
            for ( Object pck : packages ) {
              String pckId;
              String mainScript;
              if ( pck instanceof Map ) {
                pckId = (String) ( (Map) pck ).get( "name" );
                mainScript = (String) ( (Map) pck ).get( "main" );
              } else {
                pckId = (String) pck;
                mainScript = "main";
              }

              String convertedName;
              if ( !pckId.isEmpty() ) {
                convertedName = module + "_" + version + "/" + pckId;
              } else {
                convertedName = module + "_" + version;
              }

              shim.put( convertedName + "/" + mainScript, moduleShim );
            }
          }

          if ( !shim.isEmpty() ) {
            requireConfig.put( "shim", shim );
          }
        }
      }
    }

    for ( String artifact : artifacts.keySet() ) {
      final HashMap<String, HashMap<String, String>> artifactInfo = artifacts.get( artifact );

      for ( String artifactVersion : artifactInfo.keySet() ) {
        final HashMap<String, String> modules = artifactInfo.get( artifactVersion );

        final Set<String> moduleIds = modules.keySet();

        for ( String moduleId : moduleIds ) {
          String version = modules.get( moduleId );

          HashMap<String, String> moduleMap;
          if ( !map.containsKey( moduleId + "_" + version ) ) {
            moduleMap = new HashMap<>();
            map.put( moduleId + "_" + version, moduleMap );
          } else {
            moduleMap = (HashMap<String, String>) map.get( moduleId + "_" + version );
          }

          for ( String simblingModuleId : moduleIds ) {
            String simblingVersion = modules.get( moduleId );

            moduleMap.put( simblingModuleId, simblingModuleId + "_" + simblingVersion );
          }
        }
      }
    }

    if ( !map.isEmpty() ) {
      requireConfig.put( "map", map );
    }
  }

  private void processModuleRequirement( String moduleId, String moduleVersionRequeriment ) {
    ModuleRequirements moduleRequirements;

    if ( !requirements.containsKey( moduleId ) ) {
      moduleRequirements = new ModuleRequirements();
      requirements.put( moduleId, moduleRequirements );
    } else {
      moduleRequirements = requirements.get( moduleId );
    }

    if ( !moduleRequirements.groups.containsKey( moduleVersionRequeriment ) && availableModules.containsKey( moduleId ) ) {
      final ArrayList<String> resolvedVersions = resolveVersion( moduleVersionRequeriment, availableModules.get( moduleId ).keySet() );

      GroupDetail g = new GroupDetail( resolvedVersions );

      moduleRequirements.groups.put( moduleVersionRequeriment, g );

      for ( String resolvedVersionId : resolvedVersions ) {
        VersionDetail resolvedVersion;
        if ( !moduleRequirements.versions.containsKey( resolvedVersionId ) ) {
          resolvedVersion = new VersionDetail( resolvedVersionId );
          moduleRequirements.versions.put( resolvedVersionId, resolvedVersion );
        } else {
          resolvedVersion = moduleRequirements.versions.get( resolvedVersionId );
        }

        resolvedVersion.addGroup( g );
      }
    }
  }

  private ArrayList<String> resolveVersion( String versionFilter, Set<String> availableVersions ) {
    ArrayList<Version> validVersions = new ArrayList<>();
    ArrayList<String> validVersionsStrings = new ArrayList<>();

    // resolve version prematurely for exact matches to allow our own non-semantic versioning such as "7.1-SNAPSHOT"
    if ( availableVersions.contains( versionFilter ) ) {
      validVersionsStrings.add( versionFilter );
      return validVersionsStrings;
    }

    for ( String availableVersion : availableVersions ) {
      try {
        Version parsedAvailableVersion = Version.valueOf( availableVersion );

        if ( versionSatisfiesFilter( parsedAvailableVersion, versionFilter ) ) {
          validVersions.add( parsedAvailableVersion );
        }
      } catch ( Exception ignored ) {
        // Ignore
      }
    }

    if ( validVersions.isEmpty() ) {
      // Lets relax and give higher minor version if available
      try {
        Version.valueOf( versionFilter );

        return resolveVersion( "^" + versionFilter, availableVersions );
      } catch ( Exception ignored ) {
        // Ignore
      }
    } else {
      Collections.sort( validVersions, new Comparator<Version>() {
        @Override public int compare( Version v1, Version v2 ) {
          return v1.compareTo( v2 );
        }
      } );

      for ( Version v : validVersions ) {
        validVersionsStrings.add( v.toString() );
      }

    }

    return validVersionsStrings;
  }

  private boolean versionSatisfiesFilter( Version parsedAvailableVersion, String versionFilter ) {
    // Java SemVer v0.9.0's version filter expression parser doesn't handle qualifiers
    // shortcut if equals enables the most common use case (dependency with explicit version)
    // other cases like "~2.1.3-alpha.1" or ">=7.1-SNAPSHOT" will still fail until the lib is fixed
    // https://github.com/zafarkhaja/jsemver/pull/34 addresses this
    return versionFilter.equals( parsedAvailableVersion.toString() ) || parsedAvailableVersion.satisfies( versionFilter );
  }

  private class ModuleRequirements {
    HashMap<String, GroupDetail> groups;
    HashMap<String, VersionDetail> versions;

    ModuleRequirements() {
      groups = new HashMap<>();
      versions = new HashMap<>();
    }

    public void resolve() {
      ArrayList<VersionDetail> sortedVersions = new ArrayList<>( versions.values() );
      Collections.sort( sortedVersions );

      for ( VersionDetail version : sortedVersions ) {
        version.excludeYourself();
      }
    }

    public String getResolution( String versionRequirement ) {
      if ( this.groups.containsKey( versionRequirement ) ) {
        return this.groups.get( versionRequirement ).getResolvedVersion();
      }

      return null;
    }
  }

  private class VersionDetail implements Comparable<VersionDetail> {
    private final String version;
    private final HashSet<GroupDetail> groups;

    VersionDetail( String version ) {
      this.version = version;

      this.groups = new HashSet<>();
    }

    @Override public int compareTo( VersionDetail v ) {
      final int groupsCount = this.getGroupsCount();
      final int groupsCount1 = v.getGroupsCount();
      if ( groupsCount != groupsCount1 ) {
        return groupsCount > groupsCount1 ? -1 : 1;
      }

      final int uniqueVersions = this.getUniqueVersionsCount();
      final int uniqueVersions1 = v.getUniqueVersionsCount();
      if ( uniqueVersions != uniqueVersions1 ) {
        return uniqueVersions > uniqueVersions1 ? -1 : 1;
      }

      final int totalVersions = this.getVersionsCount();
      final int totalVersions1 = v.getVersionsCount();
      if ( totalVersions != totalVersions1 ) {
        return totalVersions > totalVersions1 ? -1 : 1;
      }

      return 0;
    }

    String getVersion() {
      return version;
    }

    int getGroupsCount() {
      return groups.size();
    }

    int getUniqueVersionsCount() {
      HashSet<String> uniqueVersions = new HashSet<>();

      for ( GroupDetail g : this.groups ) {
        uniqueVersions.addAll( g.getVersions() );
      }

      return uniqueVersions.size();
    }

    int getVersionsCount() {
      int count = 0;

      for ( GroupDetail g : this.groups ) {
        count += g.getVersionsCount();
      }

      return count;
    }

    public void excludeYourself() {
      for ( GroupDetail g : this.groups ) {
        g.exclude( this.version );
      }
    }

    public void addGroup( GroupDetail g ) {
      this.groups.add( g );
    }
  }

  private class GroupDetail {
    private final ArrayList<String> versions;
    private final ArrayList<String> excluded;

    GroupDetail( ArrayList<String> resolvedVersions ) {
      this.versions = resolvedVersions;

      this.excluded = new ArrayList<>();
    }

    int getVersionsCount() {
      return this.versions.size();
    }

    ArrayList<String> getVersions() {
      return versions;
    }

    void exclude( String version ) {
      if ( this.versions.size() - 1 > this.excluded.size() ) {
        this.excluded.add( version );
      }
    }

    String getResolvedVersion() {
      for ( String v : this.versions ) {
        if ( !this.excluded.contains( v ) ) {
          return v;
        }
      }

      return null;
    }
  }
}
