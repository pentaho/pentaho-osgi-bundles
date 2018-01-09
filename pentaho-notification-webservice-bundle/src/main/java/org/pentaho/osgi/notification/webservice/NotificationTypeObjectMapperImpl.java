/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
