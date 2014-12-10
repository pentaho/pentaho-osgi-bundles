package org.pentaho.osgi.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.pentaho.osgi.api.BeanFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * User: nbaker Date: 11/30/10
 */
public class BeanFactoryBlueprintImpl implements BeanFactory {
  private BlueprintContainer blueprintContainer;
  private BundleContext bundleContext;
  private Logger logger = LoggerFactory.getLogger( getClass() );

  public BeanFactoryBlueprintImpl( BlueprintContainer blueprintContainer, BundleContext bundleContext ) {
    this.blueprintContainer = blueprintContainer;
    this.bundleContext = bundleContext;
  }

  @Override
  public Object getInstance( String id ) {
    return blueprintContainer.getComponentInstance(id);
  }


  @Override
  public <T> T getInstance( String id, Class<T> classType ) {
    Object beanFromContainer = blueprintContainer.getComponentInstance( id );
    return (T) beanFromContainer;
  }
}
