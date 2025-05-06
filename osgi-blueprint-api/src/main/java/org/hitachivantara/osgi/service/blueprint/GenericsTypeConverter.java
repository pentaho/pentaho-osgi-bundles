/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.hitachivantara.osgi.service.blueprint;

import org.osgi.service.blueprint.container.Converter;
import org.osgi.service.blueprint.container.ReifiedType;

import java.util.Map;

/**
 * This converter is needed because the type erasure when injecting beans in Karaf is not following the
 * same algorithm as Java. Because of that methods which accept Generic types don't recognize the existing methods
 * as valid ones to use in the injection.
 *
 * See below issues in apache Aries project to more details:
 *  https://issues.apache.org/jira/browse/ARIES-1500
 *  https://issues.apache.org/jira/browse/ARIES-1607
 *
 * For example if you have this classes:
 * <code>
 * package my.package;
 *
 * // Generic class
 * public class GenericProperty<T> {
 *   (...)
 * }
 *
 * // The bean we want to create and set a property
 * public class MyBean {
 *   public void setStringGenericProperty(GenericProperty<String> prop) {
 *     (...)
 *   }
 * }
 *
 * // The bean which we want to inject into the MyBean instance
 * public class StringGenericProperty extends GenericProperty<String> {
 *   (...)
 * }
 * </code>
 *
 * And you define this in the blueprint:
 * <code>
 * <bean class="my.package.MyBean">
 *   <property name="stringGenericProperty">
 *     <bean class="my.package.StringGenericProperty"/>
 *   </property>
 * </bean>
 * </code>
 *
 * It will fail in the wiring because a method can't be found in MyBean class with the name "setStringGenericProperty"
 * which accepts a GenericProperty<String> instance, what is misleading because such a method exists.
 *
 * To solve the above issue we should define this in our blueprint:
 *   <type-converters>
 *     <bean class="org.hitachivantara.osgi.service.blueprint.GenericsTypeConverter">
 *       <argument>
 *         <map key-type="java.lang.Class" value-type="java.lang.Class">
 *           <entry key="my.package.StringGenericProperty" value="my.package.GenericProperty" />
 *         </map>
 *       </argument>
 *     </bean>
 *   </type-converters>
 *
 * Generic usage to declare this in your blueprint
 *   <type-converters>
 *     <bean class="org.hitachivantara.osgi.service.blueprint.GenericsTypeConverter">
 *       <argument>
 *         <map key-type="java.lang.Class" value-type="java.lang.Class">
 *           <entry key="<generic class that you want to use>" value="<generic class of the method parameter>" />
 *         </map>
 *       </argument>
 *     </bean>
 *   </type-converters>
 *
 * NOTICE: this converter does not validate that the generic types are valid.
 */
public class GenericsTypeConverter implements Converter {

  private Map<Class, Class> converterClasses;

  public GenericsTypeConverter( Map<Class, Class> converterClasses ) {
    this.converterClasses = converterClasses;
  }

  @Override
  public boolean canConvert( Object fromValue, ReifiedType toType ) {
    return converterClasses
      .keySet()
      .stream()
      .anyMatch( fromClass -> fromClass.isAssignableFrom( fromValue.getClass() ) && toType.getRawClass() == converterClasses.get( fromClass ) );
  }

  @Override
  public Object convert( Object source, ReifiedType toType ) throws Exception {
    Class convertClass = converterClasses
      .keySet()
      .stream()
      .filter( fromClass -> fromClass.isAssignableFrom( source.getClass() ) && toType.getRawClass() == converterClasses.get( fromClass ) )
      .findFirst()
      .orElseThrow( () -> new Exception( GenericsTypeConverter.class.getSimpleName()
        + " - Unable to convert from " + ( source != null ? source.getClass().getName() : "<null>" ) + " to "
        + toType.getRawClass().getName() ) );

    if ( convertClass != null ) {
      return source;
    }

    return null;
  }
}
