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

import org.junit.Test;
import org.osgi.service.blueprint.container.ReifiedType;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GenericsTypeConverterTest {

  @Test
  public void testCanConvertReturnsFalseClassIsNotFromReifiedType() {
    Map<Class, Class> converterClasses = new HashMap<>();
    GenericsTypeConverter converter = new GenericsTypeConverter( converterClasses );
    assertFalse( converter.canConvert( new A(), new ReifiedType( A.class ) ) );
    assertFalse( converter.canConvert( new B(), new ReifiedType( A.class ) ) );
  }

  @Test
  public void testCanConvertReturnsTrueClassIsReifiedType() {
    Map<Class, Class> converterClasses = new HashMap<>();
    converterClasses.put( C.class, A.class );
    GenericsTypeConverter converter = new GenericsTypeConverter( converterClasses );
    assertTrue( converter.canConvert( new C(), new ReifiedType( A.class ) ) );
  }

  @Test
  public void testConvertReturnInstanceBecauseIsReifiedTypeOfA() throws Exception {
    Map<Class, Class> converterClasses = new HashMap<>();
    converterClasses.put( C.class, A.class );
    C instance = new C();
    GenericsTypeConverter converter = new GenericsTypeConverter( converterClasses );
    assertTrue( converter.canConvert( instance, new ReifiedType( A.class ) ) );
    assertFalse( converter.canConvert( instance, new ReifiedType( B.class ) ) );
    assertEquals( instance, converter.convert( instance, new ReifiedType( A.class ) ) );
  }

  @Test( expected = Exception.class )
  public void testConvertFailsInstanceNotReifiedTypeOfA() throws Exception {
    Map<Class, Class> converterClasses = new HashMap<>();
    converterClasses.put( C.class, A.class );
    C instance = new C();
    GenericsTypeConverter converter = new GenericsTypeConverter( converterClasses );
    assertTrue( converter.canConvert( instance, new ReifiedType( A.class ) ) );
    assertFalse( converter.canConvert( instance, new ReifiedType( B.class ) ) );
    assertEquals( instance, converter.convert( instance, new ReifiedType( B.class ) ) );
  }

  private static class A<T> {
  }

  private static class B {
  }

  private static class C extends A<String> {
  }
}
