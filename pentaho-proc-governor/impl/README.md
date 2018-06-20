# Pentaho Process Governor #

The ProcessGovernor bundle provides a service used for managing execution
of external processes.  This is meant to be an alternative to raw calls
to Runtime.exec() or ProcessBuilder.start().

The ProcessGovernor will limit the maximum number of executions to a configurable
threshold, across the entire JVM.  It also provides additional logging and
structure around the launching of processes.

_Usage_

The ProcessGovernor service is registered in blueprint.  Add a reference like
```xml
<reference id="procGovernor" interface="org.hitachivantara.process.ProcessGovernor"/>

```
Process execution is then invoked with
```
CompletableFuture<Process> futureProcess = procGovernor.start( command )
``` 
If the threshold number of processes has been met, the futureProcess won't
be completed until a permit becomes available.  To determine whether
 permits are available _before_ attempting a start, use the method
```
procGovernor.availablePermits()
```

  _Alternatives considered_
 1) apache-commons-exec
 2)  https://github.com/zeroturnaround/zt-exec
 
  Both have better support for IO handling, but don't pool or limit procs.
 3) https://github.com/ViktorC/PP4J
 
 Supports pooling, but seems primarily geared at external *java* processes.  Personal project of some guy.

_For furture consideration_

1)  Use defined queueing.  It's currently arbitrary which thread will be able to launch its proc when
  a permit becomes available.
  2)  Support running procs under a different account.  Everything run under the permissions granted to the user
  running the JVM proc
  3)  Support pooling/reuse of processes.
  4)  Support  process or session mgmt.  E.g. there's no way to kill or check status of procs invoked from the governor.
  5)  Limit threads.  Each .start() request will launch a thread use to acquire a permit, start the proc and
  wait for results.  Consider replacing this design with an Actor pattern.

###Build

* Maven, version 3+
* Java JDK 1.8
* This [settings.xml](https://github.com/pentaho/maven-parent-poms/blob/master/maven-support-files/settings.xml) in your <user-home>/.m2 directory


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

__Integration tests__
In addition to the unit tests, there are integration tests in the core project.
```
$ mvn verify -DrunITs
```

To run a single integration test:
```
$ mvn verify -DrunITs -Dit.test=<<YourIT>>
```

To run a single integration test in debug mode (for remote debugging in an IDE) on the default port of 5005:
```
$ mvn verify -DrunITs -Dit.test=<<YourIT>> -Dmaven.failsafe.debug
```

__IntelliJ__

* Don't use IntelliJ's built-in maven. Make it use the same one you use from the commandline.
  * Project Preferences -> Build, Execution, Deployment -> Build Tools -> Maven ==> Maven home directory
