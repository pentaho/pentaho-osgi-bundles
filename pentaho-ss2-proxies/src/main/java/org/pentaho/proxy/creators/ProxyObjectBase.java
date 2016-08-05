package org.pentaho.proxy.creators;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProxyObjectBase {

  private Logger logger = LoggerFactory.getLogger( getClass() );

  protected Object baseTarget;
  private Method toString;

  //TODO: Consider implementing equals and hashCode
  /*
  private Method equals;
  private Method hashCode;
  */

  protected ProxyObjectBase(Object baseTarget) {
    this.baseTarget = baseTarget;
  }

  /*
   * Note: Left implementation open so objects extending can get toString without proxy object reference
   */
  public String baseTargetToString() {
    try {
      if(toString == null) {
        toString = ProxyUtils.findMethodByName(baseTarget.getClass(), "toString");
      }

      return (String) toString.invoke(baseTarget);

    } catch(IllegalAccessException | InvocationTargetException e) {
      logger.error(e.getMessage(), e);
    }
    return super.toString();
  }


  @Override
  public String toString() {
    return super.toString() + " " + baseTargetToString();
  }



}
