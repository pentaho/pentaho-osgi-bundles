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

import com.github.zafarkhaja.semver.Version;
import org.pentaho.requirejs.RequireJsPackageConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RequireJsDependencyResolver {
  public static RequireJsDependencyResolver createDependencyResolver( Collection<RequireJsPackageConfiguration> availablePackages ) {
    Map<String, Map<String, RequireJsPackageConfiguration>> packagesIndex = new HashMap<>();

    availablePackages.forEach( packageConfiguration -> {
      // if it is a nameless and/or versionless package, no other can depend on it
      if ( !packageConfiguration.getName().isEmpty() && !packageConfiguration.getVersion().isEmpty() ) {
        Map<String, RequireJsPackageConfiguration> packageVersion = packagesIndex.computeIfAbsent( packageConfiguration.getName(), name -> new HashMap<>() );
        packageVersion.putIfAbsent( packageConfiguration.getVersion(), packageConfiguration );
      }
    } );

    RequireJsDependencyResolver resolver = new RequireJsDependencyResolver( packagesIndex );
    resolver.run();

    return resolver;
  }

  private final Map<String, PackageDependentsRequirements> requirements;

  private Map<String, Map<String, RequireJsPackageConfiguration>> availablePackages;

  private RequireJsDependencyResolver( Map<String, Map<String, RequireJsPackageConfiguration>> availablePackages ) {
    this.availablePackages = availablePackages;

    this.requirements = new HashMap<>();
  }

  private void run() {
    availablePackages.values().forEach( packageInfo -> {
      packageInfo.values().forEach( versionInfo -> {
        final Map<String, String> dependencies = versionInfo.getDependencies();

        dependencies.forEach( this::processPackageDependentsRequirements );
      } );
    } );

    requirements.values().forEach( PackageDependentsRequirements::resolve );
  }

  public RequireJsPackageConfiguration getResolvedVersion( String dependencyPackageName, String dependencyPackageVersion ) {
    String resolvedVersion = requirements.get( dependencyPackageName ).getResolution( dependencyPackageVersion );

    return resolvedVersion != null ? this.availablePackages.get( dependencyPackageName ).get( resolvedVersion ) : null;
  }

  private void processPackageDependentsRequirements( String requiredPackageId, String requiredPackageVersion ) {
    PackageDependentsRequirements packageDependentsRequirements;

    if ( !requirements.containsKey( requiredPackageId ) ) {
      packageDependentsRequirements = new PackageDependentsRequirements();
      requirements.put( requiredPackageId, packageDependentsRequirements );
    } else {
      packageDependentsRequirements = requirements.get( requiredPackageId );
    }

    if ( !packageDependentsRequirements.groups.containsKey( requiredPackageVersion ) && availablePackages.containsKey( requiredPackageId ) ) {
      final ArrayList<String> resolvedVersions = resolveVersion( requiredPackageVersion, availablePackages.get( requiredPackageId ).keySet() );

      GroupDetail g = new GroupDetail( resolvedVersions );

      packageDependentsRequirements.groups.put( requiredPackageVersion, g );

      for ( String resolvedVersionId : resolvedVersions ) {
        VersionDetail resolvedVersion;
        if ( !packageDependentsRequirements.versions.containsKey( resolvedVersionId ) ) {
          resolvedVersion = new VersionDetail( resolvedVersionId );
          packageDependentsRequirements.versions.put( resolvedVersionId, resolvedVersion );
        } else {
          resolvedVersion = packageDependentsRequirements.versions.get( resolvedVersionId );
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
      validVersions.sort( Comparator.naturalOrder() );

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

  private class PackageDependentsRequirements {
    Map<String, GroupDetail> groups;
    Map<String, VersionDetail> versions;

    PackageDependentsRequirements() {
      groups = new HashMap<>();
      versions = new HashMap<>();
    }

    void resolve() {
      ArrayList<VersionDetail> sortedVersions = new ArrayList<>( versions.values() );
      Collections.sort( sortedVersions );

      for ( VersionDetail version : sortedVersions ) {
        version.excludeYourself();
      }
    }

    String getResolution( String versionRequirement ) {
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

    @Override
    public int compareTo( VersionDetail v ) {
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

    void addGroup( GroupDetail g ) {
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
