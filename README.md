# This is a repository for Pentaho's Open Source OSGI bundles

#### A note regarding the introduction of new dependencies:

##### Developers should pay special attention to the two build profiles in the project. These are "lowdeps" and "highdeps".

In simplest terms the only Pentaho dependency that can be added to any of the lowdeps modules is a dependency that is built by another lowedeps module **in this project**. Do not add any other Pentaho dependency to any of the lowdeps modules or you will break the Pentaho suite build. If you need to add a new Pentaho artifact dependency to this project, it should only be in one of the "highdeps" modules and even then, requires review of the Pentaho suite project build order to know if the dependency builds before the "highdeps" profile build of this project.
