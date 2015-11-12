package org.pentaho.js.require;

import com.github.zafarkhaja.semver.Version;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by nantunes on 12/11/15.
 */
public class RequireJsDependencyResolver {
  public static void processMetaInformation( JSONObject result ) {
    if ( !result.containsKey( "requirejs-osgi-meta" ) ) {
      return;
    }

    RequireJsDependencyResolver resolver = new RequireJsDependencyResolver( result );
    resolver.run();

    result.remove( "requirejs-osgi-meta" );
  }

  private JSONObject requireConfig;

  final HashMap<String, ModuleRequirements> requirements;
  HashMap<String, HashMap<String, HashMap<String, ?>>> availableModules;

  private RequireJsDependencyResolver( JSONObject requireConfig ) {
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

    HashMap<String, HashMap<String, HashMap<String, ?>>> artifacts;
    if ( meta.containsKey( "artifacts" ) ) {
      artifacts = (HashMap<String, HashMap<String, HashMap<String, ?>>>) meta.get( "artifacts" );
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

            if ( dependencyId.startsWith( "pentaho-webjar-deployer:" ) ) {
              final String dependencyArtifact = dependencyId.substring( 24 );

              if ( artifacts.containsKey( dependencyArtifact ) ) {
                HashMap<String, HashMap<String, ?>> artifactInfo = artifacts.get( dependencyArtifact );

                final ArrayList<String> resolvedArtifacts = resolveVersion( versionRequirement, artifactInfo.keySet() );

                for ( String artifactVersion : resolvedArtifacts ) {
                  HashMap<String, ?> artifactDetail = artifactInfo.get( artifactVersion );

                  if ( artifactDetail.containsKey( "modules" ) ) {
                    HashMap<String, String> dependencyModules = (HashMap<String, String>) artifactDetail.get( "modules" );

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

    HashMap<String, HashMap<String, ?>> map = (HashMap<String, HashMap<String, ?>>) requireConfig.get( "map" );
    for ( String module : availableModules.keySet() ) {
      final HashMap<String, HashMap<String, ?>> moduleInfo = availableModules.get( module );

      for ( String version : moduleInfo.keySet() ) {
        final HashMap<String, Object> versionInfo = (HashMap<String, Object>) moduleInfo.get( version );

        if ( versionInfo.containsKey( "processed" ) ) {
          final HashMap<String, String> processedDependencies = (HashMap<String, String>) versionInfo.get( "processed" );

          final HashMap<String, String> resolved = new HashMap<>();

          HashMap<String, String> moduleMap;
          if ( !map.containsKey( module + "/" + version ) ) {
            moduleMap = new HashMap<>();
            map.put( module + "/" + version, moduleMap );
          } else {
            moduleMap = (HashMap<String, String>) map.get( module + "/" + version );
          }

          for ( String dependencyModuleId : processedDependencies.keySet() ) {
            String dependencyModuleVersion = processedDependencies.get( dependencyModuleId );
            final String dependencyResolvedVersion = requirements.get( dependencyModuleId ).getResolution( dependencyModuleVersion );

            if ( dependencyResolvedVersion != null ) {
              resolved.put( dependencyModuleId, dependencyResolvedVersion );

              moduleMap.put( dependencyModuleId, dependencyModuleId + "/" + dependencyResolvedVersion );
            }
          }

          if ( !resolved.isEmpty() ) {
            versionInfo.put( "resolved", resolved );
          }
        }
      }
    }

    for ( String artifact : artifacts.keySet() ) {
      final HashMap<String, HashMap<String, ?>> artifactInfo = artifacts.get( artifact );

      for ( String artifactVersion : artifactInfo.keySet() ) {
        final HashMap<String, ?> versionInfo = artifactInfo.get( artifactVersion );

        if ( versionInfo.containsKey( "modules" ) ) {
          final HashMap<String, String> modules = (HashMap<String, String>) versionInfo.get( "modules" );

          for ( String module : modules.keySet() ) {
            String version = modules.get( module );

            HashMap<String, String> moduleMap;
            if ( !map.containsKey( module + "/" + version ) ) {
              moduleMap = new HashMap<>();
              map.put( module + "/" + version, moduleMap );
            } else {
              moduleMap = (HashMap<String, String>) map.get( module + "/" + version );
            }

            for ( String simblingModule : modules.keySet() ) {
              if ( !simblingModule.equals( module ) ) {
                String simblingVersion = modules.get( module );

                moduleMap.put( simblingModule, simblingModule + "/" + simblingVersion );
              }
            }
          }
        }
      }
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

  private ArrayList<String> resolveVersion( String version, Set<String> availableVersions ) {
    ArrayList<Version> validVersions = new ArrayList<>();
    ArrayList<String> validVersionsStrings = new ArrayList<>();

    for ( String availableVersion : availableVersions ) {
      try {
        Version v = Version.valueOf( availableVersion );

        if ( v.satisfies( version ) ) {
          validVersions.add( v );
        }
      } catch ( Exception ignored ) {
        // Ignore
      }
    }

    if ( validVersions.isEmpty() ) {
      // Lets relax and give higher minor version if available
      try {
        Version.valueOf( version );

        return resolveVersion( "^" + version, availableVersions );
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
