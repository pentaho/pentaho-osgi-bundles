package org.pentaho.osgi.api;

import org.osgi.framework.Bundle;

/**
 * User: nbaker
 * Date: 12/17/10
 */
public interface BeanFactoryLocator {

  BeanFactory getBeanFactory(Bundle bundle);
}
