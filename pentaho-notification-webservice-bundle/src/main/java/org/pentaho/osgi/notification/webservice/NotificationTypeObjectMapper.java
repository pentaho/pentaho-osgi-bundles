package org.pentaho.osgi.notification.webservice;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Created by bryan on 5/1/15.
 */
public interface NotificationTypeObjectMapper {
  String getType();

  ObjectMapper getObjectMapper();
}
