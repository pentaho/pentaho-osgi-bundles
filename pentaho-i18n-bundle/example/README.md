i18n Example
============

Building
--------
$: mvn install

Installing
--------
1. SSH into karaf "ssh karaf@localhost -p 8802" password: karaf. Note: port will vary
2. Install i18n bundle: "install mvn:pentaho/pentaho-i18n-bundle/7.0-SNAPSHOT"
3. Install i18n webservice bundle: "install mvn:pentaho/pentaho-i18n-webservice-bundle/7.0-SNAPSHOT"
4. Install example: "install mvn:pentaho/pentaho-i18n-bundle-example/7.0-SNAPSHOT"
Note: you may have to start the bundles:  "start 184", "start 185", etc.

Running
--------
Open browser to "http://localhost:9052/i18n/index.html". Note: your port may be different.
