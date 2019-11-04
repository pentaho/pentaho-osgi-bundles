This OSGi bundle wraps the org.ops4j.pax.logging/pax-logging-api bundle preventing it
from exporting packages from the logging APIs the Pentaho products use and inject from
the main classloader (org.apache.commons.logging, org.apache.log4j and org.slf4j).
