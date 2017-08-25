/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
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
    if( cacheManager == null ) {
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

  public void shutdown(){
    try {
      getCacheManager().shutdown();
    } catch ( Exception ignored ){
      // best effort
    }
  }

  @Override protected void finalize() throws Throwable {
    shutdown();
  }
}
