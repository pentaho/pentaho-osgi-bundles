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
