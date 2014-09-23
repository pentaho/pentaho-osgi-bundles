/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.osgi.notification.api;

/**
 * Created by bryan on 9/18/14.
 */
public class NotificationObject {
  private final String type;
  private final String id;
  private final long sequence;
  private final Object object;

  public NotificationObject( String type, String id, long sequence, Object object ) {
    this.type = type;
    this.id = id;
    this.sequence = sequence;
    this.object = object;

  }

  public String getType() {
    return type;
  }

  public String getId() {
    return id;
  }

  public Object getObject() {
    return object;
  }

  public long getSequence() {
    return sequence;
  }
}
