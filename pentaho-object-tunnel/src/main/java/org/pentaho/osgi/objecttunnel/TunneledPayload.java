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
