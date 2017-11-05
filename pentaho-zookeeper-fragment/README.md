# Hitachi Vantara Zookeeper Fragment
Apache Zookeeper's 3.4.7 Bundle manifest does not include org.apache.log4j despite using Log4J in certain classes.

This bundle adds the missing package import to Zookeeper by way of the Fragment Bundle mechanism.