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

/**
 * Static notification objects that can be used to control data-flow through the tunnel
 * <p>
 * Created by hudak on 2/14/17.
 */
public enum TunnelMarker {
  /**
   * Marks the end data sent thorugh the tunnel.
   * If the TunnelInput receives this marker, it will close the stream and call onComplete for each subscriber.
   */
  END
}
