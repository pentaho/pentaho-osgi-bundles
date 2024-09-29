/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


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
