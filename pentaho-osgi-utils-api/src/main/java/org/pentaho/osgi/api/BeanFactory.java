package org.pentaho.osgi.api;

/**
 * User: nbaker
 * Date: 11/30/10
 */
public interface BeanFactory {

  public Object getInstance(String id);

  public <T> T getInstance(String id, Class<T> classType);
}
