/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
