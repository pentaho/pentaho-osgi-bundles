# This is a repository for Pentaho's Open Source OSGI bundles

#### A note regarding the introduction of new dependencies:

##### Developers should pay special attention to the two build profiles in the project. These are "lowdeps" and "highdeps".

In simplest terms the only Pentaho dependency that can be added to any of the lowdeps modules is a dependency that is built by another lowedeps module **in this project**. Do not add any other Pentaho dependency to any of the lowdeps modules or you will break the Pentaho suite build. If you need to add a new Pentaho artifact dependency to this project, it should only be in one of the "highdeps" modules and even then, requires review of the Pentaho suite project build order to know if the dependency builds before the "highdeps" profile build of this project.


#### Pre-requisites for building the project:
* Maven, version 3+
* Java JDK 1.8
* This [settings.xml](https://github.com/pentaho/maven-parent-poms/blob/master/maven-support-files/settings.xml) in your <user-home>/.m2 directory

#### Building it

__Build for nightly/release__

All required profiles are activated by the presence of a property named "release".

```
$ mvn clean install -Drelease
```

This will build, unit test, and package the whole project (all of the sub-modules). The artifact will be generated in: ```target```

__Build for CI/dev__

The `release` builds will compile the source for production (meaning potential obfuscation and/or uglification). To build without that happening, just eliminate the `release` property.

```
$ mvn clean install
```

#### Running the tests

__Unit tests__

This will run all tests in the project (and sub-modules).
```
$ mvn test
```

If you want to remote debug a single java unit test (default port is 5005):
```
$ cd core
$ mvn test -Dtest=<<YourTest>> -Dmaven.surefire.debug
```


__IntelliJ__

* Don't use IntelliJ's built-in maven. Make it use the same one you use from the commandline.
  * Project Preferences -> Build, Execution, Deployment -> Build Tools -> Maven ==> Maven home directory

