package org.pentaho.platform.proxy.api;

/**
 * Instances of this class will respond to requests to create a Proxy object for a given target. Created by nbaker on
 * 8/9/15.
 */
public interface IProxyCreator<T> {

  /**
   * Asked whether the creator supports creating proxies for the given class
   *
   * @param clazz
   * @return true if supports class
   */
  boolean supports( Class<?> clazz );

  /**
   * Call to create a Proxy Object to be used instead of the target object.
   *
   * @param target
   * @return
   */
  T create( Object target );
}
