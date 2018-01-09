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
package org.pentaho.osgi.objecttunnel;

import java.io.Serializable;

/**
 * A simple Struct containing the "type" of the tunneled Object along with the serialized String form of the object.
 * Instances of these are sent thru the Object Streams and read by TunnelInput.
 * <p>
 * Created by nbaker on 2/6/17.
 */
public class TunneledPayload implements Serializable {
  private String type;
  private String objectStr;

  public TunneledPayload( String id, String objectStr ) {
    this.type = id;
    this.objectStr = objectStr;
  }

  public String getType() {
    return type;
  }

  public String getObjectStr() {
    return objectStr;
  }

}
