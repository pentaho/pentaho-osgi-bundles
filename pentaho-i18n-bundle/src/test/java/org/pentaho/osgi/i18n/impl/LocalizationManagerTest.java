/*!
 * Copyright 2010 - 2024 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.osgi.i18n.impl;

import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.pentaho.osgi.i18n.resource.OSGIResourceBundle;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bryan on 9/5/14.
 */
public class LocalizationManagerTest {
  private Logger log;
  private Logger cachedLogger;
  private LocalizationManager localizationManager;
  private ExecutorService executorService;

  @Before
  public void setup() {
    log = mock( Logger.class );
    cachedLogger = LocalizationManager.getLog();
    localizationManager = new LocalizationManager();
    executorService = mock( ExecutorService.class );
  }

  @After
  public void teardown() {
    LocalizationManager.setLog( cachedLogger );
  }

  @Test
  public void testLocalizationManager() throws IOException, ParseException {
    localizationManager.setExecutorService( executorService );
    assertBundleNullCacheNull( "messages", "de", "DE" );

    assertBundleNullCacheNullRegexp( Pattern.compile( "messages" ),
      "de", "DE" );

    localizationManager.bundleChanged( makeMockBundleNull( 1L, Bundle.ACTIVE ) );
    assertBundleNullCacheNull( "messages", "de", "DE" );
    assertBundleNullCacheNull( null, "de", "DE" );
    assertBundleNullCacheNull( "messages", null );

    localizationManager.setExecutorService( null );
    assertBundleNullCacheNull( "messages", "de", "DE" );
    assertBundleNullCacheNull( null, "de", "DE" );
    assertBundleNullCacheNull( "messages", null );
    localizationManager.bundleChanged( makeMockBundle( 1L, Bundle.ACTIVE, "i18n/bundle/messages.properties",
      "i18n/bundle/messages_fr.properties", "i18n/bundle/messages_de_DE.properties" ) );
    assertBundleNullCacheNull( null, "de", "DE" );
    assertBundleNullCacheNull( "", "de", "DE" );
    assertBundleKeyEquals( "defaultKey", "bundle/messages", "defaultKey", "fakeLocale" );
    assertBundleKeyEquals( "key", "bundle/messages", "key", "fakeLocale" );
    assertBundleKeyEquals( "key_fr", "bundle/messages", "key", "fr" );
    assertBundleKeyEquals( "defaultKey", "bundle/messages", "defaultKey", "fr" );
    assertBundleKeyEquals( "key_fr", "bundle/messages", "key", "fr", "FR" );
    assertBundleKeyEquals( "defaultKey", "bundle/messages", "defaultKey", "fr", "FR" );
    assertBundleKeyEquals( "key", "bundle/messages", "key", "de" );
    assertBundleKeyEquals( "defaultKey", "bundle/messages", "defaultKey", "de" );
    assertBundleKeyEquals( "key_de_DE", "bundle/messages", "key", "de", "DE" );
    assertBundleKeyEquals( "defaultKey", "bundle/messages", "defaultKey", "de", "DE" );

    assertBundlePatternKeyEquals( "defaultKey", Pattern.compile( ".*messages" ), "defaultKey", "fakeLocale" );
    assertBundlePatternKeyEquals( "key", Pattern.compile( ".*messages" ), "key", "fakeLocale" );
    assertBundlePatternKeyEquals( "key_fr", Pattern.compile( ".*messages" ), "key", "fr" );
    assertBundlePatternKeyEquals( "defaultKey", Pattern.compile( ".*messages" ), "defaultKey", "fr" );
    assertBundlePatternKeyEquals( "key_fr", Pattern.compile( ".*messages" ), "key", "fr", "FR" );
    assertBundlePatternKeyEquals( "defaultKey", Pattern.compile( ".*messages" ), "defaultKey", "fr", "FR" );
    assertBundlePatternKeyEquals( "key", Pattern.compile( ".*messages" ), "key", "de" );
    assertBundlePatternKeyEquals( "defaultKey", Pattern.compile( ".*messages" ), "defaultKey", "de" );
    assertBundlePatternKeyEquals( "key_de_DE", Pattern.compile( ".*messages" ), "key", "de", "DE" );
    assertBundlePatternKeyEquals( "defaultKey", Pattern.compile( ".*messages" ), "defaultKey", "de", "DE" );

    localizationManager.bundleChanged( makeMockBundle( 2L, Bundle.ACTIVE, "i18n/bundle/messages_fr.properties.2" ) );
    assertBundleKeyEquals( "key_fr_bundle2", "bundle/messages", "key", "fr" );
    assertBundleKeyEquals( "defaultKey", "bundle/messages", "defaultKey", "fr" );
    assertBundleKeyEquals( "key for priority 2", "bundle/messages", "key2", "fr" );

    assertBundlePatternKeyEquals( "key_fr_bundle2", Pattern.compile( ".*messages" ), "key", "fr" );
    assertBundlePatternKeyEquals( "defaultKey", Pattern.compile( ".*messages" ), "defaultKey", "fr" );
    assertBundlePatternKeyEquals( "key for priority 2", Pattern.compile( ".*messages" ), "key2", "fr" );

    //test bundle 2L stopping
    localizationManager.bundleChanged( makeMockBundle( 2L, Bundle.RESOLVED, "i18n/bundle/messages_fr.properties.2" ) );
    assertBundleKeyEquals( "key_fr", "bundle/messages", "key", "fr" );
    assertBundleKeyEquals( "defaultKey", "bundle/messages", "defaultKey", "fr" );
    assertBundlePatternKeyEquals( "key_fr", Pattern.compile( ".*messages" ), "key", "fr" );
    assertBundlePatternKeyEquals( "defaultKey", Pattern.compile( ".*messages" ), "defaultKey", "fr" );

    localizationManager.bundleChanged( makeMockBundle( 3L, Bundle.ACTIVE, "i18n/bundle/messages_fr.properties.3" ) );
    assertBundleKeyEquals( "key_fr_bundle3", "bundle/messages", "key", "fr" );
    assertBundleKeyEquals( "defaultKey", "bundle/messages", "defaultKey", "fr" );
    assertBundleKeyEquals( "key for priority 3", "bundle/messages", "key3", "fr" );

    assertBundlePatternKeyEquals( "key_fr_bundle3", Pattern.compile( ".*messages" ), "key", "fr" );
    assertBundlePatternKeyEquals( "defaultKey", Pattern.compile( ".*messages" ), "defaultKey", "fr" );
    assertBundlePatternKeyEquals( "key for priority 3", Pattern.compile( ".*messages" ), "key3", "fr" );

    localizationManager.bundleChanged( makeMockBundle( 4L, Bundle.ACTIVE, "i18n/bundle/messages_fr.properties.4" ) );
    assertBundleKeyEquals( "key_fr_bundle4", "bundle/messages", "key", "fr" );
    assertBundleKeyEquals( "defaultKey", "bundle/messages", "defaultKey", "fr" );
    assertBundleKeyEquals( "key for priority 3", "bundle/messages", "key3", "fr" );

    assertBundlePatternKeyEquals( "key_fr_bundle4", Pattern.compile( ".*messages" ), "key", "fr" );
    assertBundlePatternKeyEquals( "defaultKey", Pattern.compile( ".*messages" ), "defaultKey", "fr" );
    assertBundlePatternKeyEquals( "key for priority 3", Pattern.compile( ".*messages" ), "key3", "fr" );

    localizationManager.bundleChanged( makeMockBundle( 5L, Bundle.ACTIVE, "fakepath" ) );
    assertBundleKeyEquals( "defaultKey", "bundle/messages", "defaultKey", "fakeLocale" );
    assertBundleKeyEquals( "key", "bundle/messages", "key", "fakeLocale" );
    assertBundleKeyEquals( "key_fr_bundle4", "bundle/messages", "key", "fr" );
    assertBundleKeyEquals( "defaultKey", "bundle/messages", "defaultKey", "fr" );
    assertBundleKeyEquals( "key_fr_bundle4", "bundle/messages", "key", "fr", "FR" );
    assertBundleKeyEquals( "defaultKey", "bundle/messages", "defaultKey", "fr", "FR" );
    assertBundleKeyEquals( "key", "bundle/messages", "key", "de" );
    assertBundleKeyEquals( "defaultKey", "bundle/messages", "defaultKey", "de" );
    assertBundleKeyEquals( "key_de_DE", "bundle/messages", "key", "de", "DE" );
    assertBundleKeyEquals( "defaultKey", "bundle/messages", "defaultKey", "de", "DE" );

    assertBundlePatternKeyEquals( "defaultKey", Pattern.compile( ".*messages" ), "defaultKey", "fakeLocale" );
    assertBundlePatternKeyEquals( "key", Pattern.compile( ".*messages" ), "key", "fakeLocale" );
    assertBundlePatternKeyEquals( "key_fr_bundle4", Pattern.compile( ".*messages" ), "key", "fr" );
    assertBundlePatternKeyEquals( "defaultKey", Pattern.compile( ".*messages" ), "defaultKey", "fr" );
    assertBundlePatternKeyEquals( "key_fr_bundle4", Pattern.compile( ".*messages" ), "key", "fr", "FR" );
    assertBundlePatternKeyEquals( "defaultKey", Pattern.compile( ".*messages" ), "defaultKey", "fr", "FR" );
    assertBundlePatternKeyEquals( "key", Pattern.compile( ".*messages" ), "key", "de" );
    assertBundlePatternKeyEquals( "defaultKey", Pattern.compile( ".*messages" ), "defaultKey", "de" );
    assertBundlePatternKeyEquals( "key_de_DE", Pattern.compile( ".*messages" ), "key", "de", "DE" );
    assertBundlePatternKeyEquals( "defaultKey", Pattern.compile( ".*messages" ), "defaultKey", "de", "DE" );
    assertBundlePatternKeyEquals( "defaultKey", Pattern.compile( ".*messages" ), "defaultKey", "" );
  }

  @Test
  public void testLocalosationManagerWrongCache()
    throws IOException, ParseException, ExecutionException, InterruptedException {
    localizationManager.setExecutorService( mockExecutorServiceWithCacheTrowingError() );
    localizationManager.bundleChanged( makeMockBundle( 2L, Bundle.ACTIVE, "i18n/bundle/messages_fr.properties.2" ) );
    assertBundleNullCacheNull("bundle/messages", "key", "fr" );
  }

  private ExecutorService mockExecutorServiceWithCacheTrowingError() throws ExecutionException, InterruptedException {
    Future<Map<String, OSGIResourceBundle>> mockF = mock( Future.class );
    when( mockF.get() ).thenThrow( InterruptedException.class );
    ExecutorService service = mock( ExecutorService.class );
    when( service.submit( any( Callable.class ) ) ).thenReturn( mockF );
    return service;
  }

  private void assertBundleKeyEquals( String expected, String key, String valueKey,
                                      String... localeStrings ) {
    Locale locale = null;
    if ( localeStrings.length == 1 ) {
      locale = new Locale( localeStrings[ 0 ] );
    } else if ( localeStrings.length == 2 ) {
      locale = new Locale( localeStrings[ 0 ], localeStrings[ 1 ] );
    } else {
      throw new RuntimeException( "Expected either 1 or 2 locale strings" );
    }
    assertEquals( expected, localizationManager.getResourceBundle( key, locale ).getString( valueKey ) );
  }

  private void assertBundlePatternKeyEquals( String expected, Pattern keyRegex, String valueKey,
                                             String... localeStrings ) {
    Locale locale = null;
    if ( localeStrings.length == 1 ) {
      locale = new Locale( localeStrings[ 0 ] );
    } else if ( localeStrings.length == 2 ) {
      locale = new Locale( localeStrings[ 0 ], localeStrings[ 1 ] );
    } else {
      throw new RuntimeException( "Expected either 1 or 2 locale strings" );
    }
    assertEquals( expected,
      localizationManager.getResourceBundles( keyRegex, locale ).get( 0 ).getString( valueKey ) );
  }

  private void assertBundleNullCacheNull( String key, String... localeStrings ) {
    Locale locale = null;

    if ( localeStrings != null ) {
      if ( localeStrings.length == 1 ) {
        locale = new Locale( localeStrings[ 0 ] );
      } else if ( localeStrings.length == 2 ) {
        locale = new Locale( localeStrings[ 0 ], localeStrings[ 1 ] );
      } else {
        throw new RuntimeException( "Expected either 1 or 2 locale strings" );
      }
    }
    assertNull( localizationManager.getResourceBundle( key, locale ) );

  }

  private void assertBundleNullCacheNullRegexp( Pattern keyRegex, String... localeStrings ) {
    Locale locale = null;

    if ( localeStrings != null ) {
      if ( localeStrings.length == 1 ) {
        locale = new Locale( localeStrings[ 0 ] );
      } else if ( localeStrings.length == 2 ) {
        locale = new Locale( localeStrings[ 0 ], localeStrings[ 1 ] );
      } else {
        throw new RuntimeException( "Expected either 1 or 2 locale strings" );
      }
    }
    assertNull( localizationManager.getResourceBundles( keyRegex, locale ) );

  }

  private Bundle makeMockBundle( Long bundleId, int bundleStatus, String... propertiesPaths ) {
    Bundle bundle = mock( Bundle.class );
    when( bundle.getState() ).thenReturn( bundleStatus );
    when( bundle.getBundleId() ).thenReturn( bundleId );
    List<URL> propertiesFiles = new ArrayList<URL>();
    for ( String path : propertiesPaths ) {
      propertiesFiles.add( getClass().getClassLoader().getResource( path ) );
    }
    when( bundle.findEntries( "i18n", "*.properties*", true ) )
      .thenReturn( new Vector<URL>( propertiesFiles ).elements() );
    return bundle;
  }

  private Bundle makeMockBundleNull( Long bundleId, int bundleStatus ) {
    Bundle bundle = mock( Bundle.class );
    when( bundle.getState() ).thenReturn( bundleStatus );
    when( bundle.getBundleId() ).thenReturn( bundleId );
    when( bundle.findEntries( "i18n", "*.properties*", true ) ).thenReturn( null );
    return bundle;
  }
}
