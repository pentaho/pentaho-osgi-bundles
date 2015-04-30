package org.pentaho.caching.ri;

import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.caching.api.PentahoCacheSystemConfiguration;
import org.pentaho.caching.ri.impl.GuavaCacheManager;

import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * @author nhudak
 */
@RunWith( MockitoJUnitRunner.class )
public class HeapCacheProvidingServiceTest {

  @Mock private PentahoCacheSystemConfiguration config;
  private HeapCacheProvidingService service;

  @Before
  public void setUp() throws Exception {
    service = new HeapCacheProvidingService();
  }

  @Test
  public void testCreateCacheManager() throws Exception {
    CacheManager cacheManager = service.createCacheManager( config );
    assertThat( cacheManager, instanceOf( GuavaCacheManager.class ) );
  }

  @Test
  public void testCreateConfiguration() throws Exception {
    HashMap<String, String> properties = Maps.newHashMap();
    Configuration<String, List> configuration = service.createConfiguration( String.class, List.class, properties );
    assertThat( configuration.getKeyType(), equalTo( String.class ) );
    assertThat( configuration.getValueType(), equalTo( List.class ) );
  }
}
