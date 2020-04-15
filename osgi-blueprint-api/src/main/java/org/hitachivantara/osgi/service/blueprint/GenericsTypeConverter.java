/*
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2020 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */

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
 * https://issues.apache.org/jira/browse/ARIES-1500
 * https://issues.apache.org/jira/browse/ARIES-1607
 *
 * To use declare this in your blueprint
 *   <type-converters>
 *     <bean class="org.hitachivantara.osgi.service.blueprint.GenericsTypeConverter">
 *       <argument>
 *         <map key-type="java.lang.Class" value-type="java.lang.Class">
 *           <entry key="<generic class that you want to use>" value="<generic class of the method parameter>" />
 *         </map>
 *       </argument>
 *     </bean>
 *   </type-converters>
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
