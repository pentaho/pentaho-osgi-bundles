package org.pentaho.osgi.notification.webservice;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Created by bryan on 5/1/15.
 */
public class NotificationTypeObjectMapperImpl implements NotificationTypeObjectMapper {
  private final String type;
  private final ObjectMapper objectMapper;

  public NotificationTypeObjectMapperImpl( ObjectMapper objectMapper, String type ) {
    this.objectMapper = objectMapper;
    this.type = type;
  }

  @Override public String getType() {
    return type;
  }

  @Override public ObjectMapper getObjectMapper() {
    return objectMapper;
  }
}
