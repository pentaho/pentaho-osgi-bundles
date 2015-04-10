package org.pentaho.capabilities.api;

import java.net.URI;
import java.util.Locale;
import java.util.concurrent.Future;

/**
 * Created by nbaker on 4/6/15.
 */
public interface ICapability {
  String getId();
  String getDescription( Locale locale );
  boolean isInstalled();
  Future<Boolean> install();
  Future<Boolean> uninstall();
  URI getSourceUri();
}
