package org.pentaho.caching.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.pentaho.caching.api.PentahoCacheManager;
import org.pentaho.caching.api.PentahoCacheProvidingService;
import org.pentaho.caching.api.PentahoCacheSystemConfiguration;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.osgi.framework.Constants.OBJECTCLASS;
import static org.osgi.framework.Constants.SERVICE_PID;
import static org.pentaho.caching.api.Constants.PENTAHO_CACHE_PROVIDER;
import static org.pentaho.caching.api.Constants.convertDictionary;

@RunWith( MockitoJUnitRunner.class )
public class PentahoCacheManagerFactoryTest {

  public static final String PROVIDER_ID = "mockProvider";

  public static final String MOCK_PID = "org.pentaho.caching-mock";
  public static final String VERSION_PROPERTY = "revision";

  private final Matcher<String> matchesFilter = allOf(
    containsString( OBJECTCLASS + "=" + PentahoCacheProvidingService.class.getName() ),
    containsString( PENTAHO_CACHE_PROVIDER + "=*" )
  );

  private PentahoCacheManagerFactory factory;
  @Mock private BundleContext bundleContext;
  @Mock private ServiceRegistration<Object> registration;
  @Mock private PentahoCacheProvidingService providingService;
  @Mock private ServiceReference<PentahoCacheProvidingService> serviceReference;
  @Captor private ArgumentCaptor<PentahoCacheManager> cacheManagerCaptor;
  @Captor private ArgumentCaptor<Dictionary<String, String>> propertiesCaptor;
  @Captor private ArgumentCaptor<ServiceListener> serviceListenerCaptor;

  @Before
  public void setUp() throws Exception {
    factory = new PentahoCacheManagerFactory( bundleContext );
    doReturn( registration ).when( bundleContext ).registerService(
      eq( PentahoCacheManager.class ),
      any( PentahoCacheManager.class ),
      argThat( new TypeSafeMatcher<Dictionary<String, ?>>() {
        @Override public void describeTo( Description description ) {
          description.appendText( "Dictionary with " ).appendText( SERVICE_PID ).appendText( " property" );
        }

        @Override protected boolean matchesSafely( Dictionary<String, ?> dictionary ) {
          return dictionary.get( SERVICE_PID ) != null;
        }
      } )
    );

    when( bundleContext.getServiceReferences( eq( PentahoCacheProvidingService.class ), argThat( matchesFilter ) ) )
      .thenReturn( ImmutableList.of( serviceReference ) );
    when( serviceReference.getProperty( PENTAHO_CACHE_PROVIDER ) ).thenReturn( MOCK_PID );
    when( bundleContext.getService( serviceReference ) ).thenReturn( providingService );
  }

  @Test
  public void testServiceLifecycleTest() throws Exception {
    // Initial configuration for an unavailable service
    Hashtable<String, String> cfg = new Hashtable<String, String>();
    cfg.put( PENTAHO_CACHE_PROVIDER, "unavailable" );
    cfg.put( "global." + VERSION_PROPERTY, "0" );
    factory.updated( MOCK_PID, cfg );

    // Register Service
    factory.registerProvider( PROVIDER_ID, providingService );

    // No services should start
    verifyNoMoreInteractions( bundleContext, registration );

    // Update config to use provider
    cfg.put( PENTAHO_CACHE_PROVIDER, PROVIDER_ID );
    cfg.put( "global." + VERSION_PROPERTY, "1" );
    factory.updated( MOCK_PID, cfg );

    // Verify that the service started with this provider
    verify( bundleContext ).registerService(
      eq( PentahoCacheManager.class ),
      cacheManagerCaptor.capture(), propertiesCaptor.capture() );
    PentahoCacheManager cacheManager = cacheManagerCaptor.getValue();
    PentahoCacheSystemConfiguration systemConfiguration = cacheManager.getSystemConfiguration();

    assertThat( systemConfiguration.getGlobalProperties().get( VERSION_PROPERTY ), equalTo( "1" ) );
    assertThat( convertDictionary( propertiesCaptor.getValue() ), equalTo( (Map<String, String>) ImmutableMap.of(
      SERVICE_PID, MOCK_PID,
      PENTAHO_CACHE_PROVIDER, PROVIDER_ID
    ) ) );

    // Update configuration
    cfg.put( "global." + VERSION_PROPERTY, "2" );
    factory.updated( MOCK_PID, cfg );
    assertThat( systemConfiguration.getGlobalProperties().get( VERSION_PROPERTY ), equalTo( "2" ) );

    // Remove provider
    factory.unregisterProvider( PROVIDER_ID, providingService );

    // Service should stop and wait
    verify( registration ).unregister();

    // Remove configuration
    factory.deleted( MOCK_PID );

    // Restore service, should have no effect
    factory.registerProvider( PROVIDER_ID, providingService );
    verifyNoMoreInteractions( bundleContext, registration );
  }

  @Test
  public void testListeners() throws Exception {
    Future<PentahoCacheProvidingService> serviceFuture;

    factory.init();
    verify( bundleContext ).addServiceListener( serviceListenerCaptor.capture(), argThat( matchesFilter ) );

    serviceFuture = factory.getProviderService( MOCK_PID );
    assertThat( serviceFuture.isDone(), is( true ) );
    assertThat( serviceFuture.get( 1, TimeUnit.SECONDS ), is( providingService ) );

    ServiceListener serviceListener = serviceListenerCaptor.getValue();

    serviceListener.serviceChanged( new ServiceEvent( ServiceEvent.UNREGISTERING, serviceReference ) );

    serviceFuture = factory.getProviderService( MOCK_PID );
    assertThat( serviceFuture.isDone(), is( false ) );

    serviceListener.serviceChanged( new ServiceEvent( ServiceEvent.REGISTERED, serviceReference ) );

    serviceFuture = factory.getProviderService( MOCK_PID );
    assertThat( serviceFuture.isDone(), is( true ) );
    assertThat( serviceFuture.get( 1, TimeUnit.SECONDS ), is( providingService ) );
  }
}
