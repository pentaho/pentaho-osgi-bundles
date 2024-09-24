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

package org.pentaho.platform.platform.osgi.requirejs.bindings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.platform.osgi.requirejs.bindings.RequireJsConfig;
import org.pentaho.platform.osgi.requirejs.bindings.Shim;
import org.pentaho.platform.osgi.requirejs.compressor.bindings.CompressorConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class RequireJsConfigTest {

  static ObjectMapper mapper = new ObjectMapper(); // create once, reuse

  @BeforeClass
  public static void setup(){

    mapper.enable( SerializationFeature.INDENT_OUTPUT );
    // to allow serialization of "empty" POJOs (no properties to serialize)
    // (without this setting, an exception is thrown in those cases)
    mapper.disable( SerializationFeature.FAIL_ON_EMPTY_BEANS );

    // DeserializationFeature for changing how JSON is read as POJOs:

    // Be lenient about properties we aren't prepared for
    mapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
    // to allow coercion of JSON empty String ("") to null Object value:
    mapper.enable( DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT );
    mapper.enable( DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY );

    // to allow C/C++ style comments in JSON (non-standard, disabled by default)
    mapper.configure( JsonParser.Feature.ALLOW_COMMENTS, true );
    //    // to allow (non-standard) unquoted field names in JSON:
    mapper.configure( JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true );
    //    // to allow use of apostrophes (single quotes), non standard
    mapper.configure( JsonParser.Feature.ALLOW_SINGLE_QUOTES, true );
    mapper.configure( JsonGenerator.Feature.ESCAPE_NON_ASCII, true );
    mapper.setSerializationInclusion( JsonInclude.Include.NON_NULL);


//    mapper.setPropertyNamingStrategy( PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES );
  }

  @Test
  public void testCompressorConfig() throws Exception {

    CompressorConfig config = mapper.readValue( new File( "src/test/resources/testCompressorConfig.js" ), CompressorConfig.class );
    mapper.writeValue( System.out, config );
    assertEquals( "./module-scripts", config.getAppDir() );
  }

  @Test
  public void testModuleMapping() throws Exception {


    RequireJsConfig requireJsConfig = mapper.readValue( new File( "src/test/resources/testConfig.js" ), RequireJsConfig.class );

    assertEquals( "/Public/js", requireJsConfig.getBaseUrl() );
    assertEquals( 3, requireJsConfig.getWaitSeconds() );
    HashMap<String, String> paths = requireJsConfig.getPaths();
    assertNotNull( paths );
    assertTrue( paths.size() > 0 );
    assertTrue( paths.containsKey( "jqueryui" ) );
    assertTrue( paths.containsKey( "jquery" ) );
    assertEquals( "../../Scripts/jquery-ui-1.10.2.min", paths.get( "jqueryui" ) );
    assertEquals( "../../Scripts/jquery-1.10.2.min", paths.get( "jquery" ) );

    HashMap<String, Shim> shim = requireJsConfig.getShim();
    assertNotNull( shim );
    assertEquals( 1, shim.size() );

    Shim jqueryui = shim.get( "jqueryui" );
    assertNotNull( jqueryui );
    assertEquals( 1, jqueryui.getDeps().length );
    assertEquals( "jquery", jqueryui.getDeps()[ 0 ] );

    // Serialize it back out and then in again to compare.
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );

    mapper.writeValue( outputStream, requireJsConfig );

    String serialized = new String( outputStream.toByteArray(), "UTF-8" );

    RequireJsConfig configRoundTripped = mapper.readValue( serialized, RequireJsConfig.class );

    assertEquals( requireJsConfig, configRoundTripped);

  }
}