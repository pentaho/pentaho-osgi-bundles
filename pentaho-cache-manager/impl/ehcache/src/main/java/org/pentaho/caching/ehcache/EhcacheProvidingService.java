/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.caching.ehcache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import org.ehcache.jcache.JCacheCachingProvider;
import org.ehcache.jcache.JCacheManager;
import org.pentaho.caching.api.PentahoCacheSystemConfiguration;
import org.pentaho.caching.spi.AbstractCacheProvidingService;

import java.net.URI;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 *  PentahoCacheProvidingService implementation which leverages the
 *  org.ehcache.jcache project to provide a JCache CacheManager wrapping
 *  ehcache 2.x. Once we move to ehcache 3.x we'll no longer need the wrapper.
 */
public class EhcacheProvidingService extends AbstractCacheProvidingService {

  private static final JCacheCachingProvider providerInstance = new JCacheCachingProvider();
  private static CacheManager cacheManager;

  private final ExecutorService executorService = Executors.newSingleThreadExecutor( new ThreadFactory() {
    private final ThreadFactory threadFactory = Executors.defaultThreadFactory();

    @Override public Thread newThread( Runnable r ) {
      Thread thread = threadFactory.newThread( r );
      thread.setName( "ehcache-jcache" );
      thread.setDaemon( true );
      return thread;
    }
  } );

  public static CacheManager getCacheManager() {
    if ( cacheManager == null ) {
      cacheManager = CacheManager.newInstance( new Configuration() );
    }
    return cacheManager;
  }

  @Override public javax.cache.CacheManager createCacheManager( PentahoCacheSystemConfiguration systemConfiguration ) {

    return new JCacheManager(
        providerInstance,
        getCacheManager(),
        URI.create( getClass().getName() ),
      new Properties() ) {
      @Override public ExecutorService getExecutorService() {
        return executorService;
      }
    };
  }

  public void shutdown() {
    try {
      getCacheManager().shutdown();
    } catch ( Exception ignored ) {
      // best effort
    }
  }

  @Override protected void finalize() throws Throwable {
    shutdown();
  }
}
