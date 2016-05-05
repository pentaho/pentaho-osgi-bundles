i18n Example
============

Building
--------
$: mvn install

Installing
--------
1. SSH into karaf "ssh karaf@localhost -p 8802" password: karaf. Note: port will vary
2. Install i18n bundle: "install mvn:pentaho/pentaho-i18n-bundle/7.0-SNAPSHOT"
3. Install example: "install mvn:pentaho/pentaho-i18n-bundle-example/7.0-SNAPSHOT"

Running
--------
Open browser to "http://localhost:9052/i18n/index.html". Note: your port may be different.
