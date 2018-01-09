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
 * A Tuple Struct class containing the "type" of the Tunneled Object along with the raw Object itself. These are
 * returned from a TunnelInput
 * <p>
 * Created by nbaker on 2/6/17.
 */
public class TunneledInputObject implements Serializable {
  private String type;
  private Object object;

  public TunneledInputObject( String type, Object object ) {
    this.type = type;
    this.object = object;
  }

  public String getType() {
    return type;
  }

  public Object getObject() {
    return object;
  }
}
