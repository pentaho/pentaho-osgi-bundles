package org.pentaho.proxy.creators;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.platform.proxy.impl.ProxyException;

import java.lang.reflect.Method;

public class ProxyUtils {

  private static ProxyUtils singleton;

  private IProxyFactory proxyFactory;

  protected ProxyUtils() {
    proxyFactory = PentahoSystem.get( IProxyFactory.class );
  }

  public static ProxyUtils getInstance() {
    if( singleton == null ){
      singleton = new ProxyUtils();
    }
    return singleton;
  }

  // instance methods

  public IProxyFactory getProxyFactory() {
    return proxyFactory;
  }

  // static methods

  public static Method findMethodByName( Class clazz, String methodName ){
    return findMethodByName( clazz, methodName, new Class[] {} /* zero-args method */ );
  }

  public static Method findMethodByName( Class clazz, String methodName, Class... arguments ){
    for ( Method method : clazz.getMethods() ) {
      if( method.getName().equals( methodName ) ){
        for ( int i = 0; i < arguments.length; i++ ) {
          if( method.getParameterTypes()[ i ].isAssignableFrom( arguments[i] ) == false ){
            return null;
          }
        }
        return method;
      }
    }
    return null;
  }

  public static boolean isRecursivelySupported( final String supportedClassName, Class aClass  ) {

    if( aClass != null && supportedClassName != null && !supportedClassName.isEmpty() ) {

      // 1. the class itself is the 'supportedClassName'
      if( supportedClassName.equals( aClass.getName() ) ){
        return true;

      }

      // 2. one of its interfaces is the 'supportedClassName'
      if( aClass.getInterfaces().length > 0 ){

        for( Class c : aClass.getInterfaces() ) {

          if( c.getName().equals( supportedClassName ) ){
            return true;
          }
        }
      }

      // 3. one of its superclasses is the 'supportedClassName' ( recursive check )
      if( aClass.getSuperclass() != null ) {
        return isRecursivelySupported( supportedClassName, aClass.getSuperclass() );
      }

    }

    // nope
    return false;
  }
}
