## Pentaho Capability Manager
The Capability Manager is an abstraction over the various plugin systems available throughout Pentaho. The intention is not to try to provide a new way of interactive with these systems directly, but rather provide a very 
high-level abstraction above these systems for listing capabilities which are installed, available and providing a simple way to install and uninstall at runtime.

Capabilities may be Traditional PDI Plugins, Platform Plugins, OSGI Bundles, Karaf Features, anything which can be managed and considered an atomic unit providing some capability to the system.
  
## CapabilityManager
Interacting with the Capability system is done through the ICapabilityManager implementation in effect. There's a simple implementation of this interface available within this project. At the 
simplest the manager simply aggregates all capabilities found via the registered ICapabilityProvider instances.
 
## Capability Providers
Implementations of ICapabilityProvider are the touchpoint between the agnostic Capability Manager APIs and the various underlying systems.