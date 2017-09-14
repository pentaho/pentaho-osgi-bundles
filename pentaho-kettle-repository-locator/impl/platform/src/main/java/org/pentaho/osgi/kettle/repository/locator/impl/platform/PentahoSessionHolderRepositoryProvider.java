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

package org.pentaho.osgi.kettle.repository.locator.impl.platform;

import org.pentaho.di.repository.Repository;
import org.pentaho.osgi.kettle.repository.locator.api.KettleRepositoryProvider;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by bryan on 3/28/16.
 */
public class PentahoSessionHolderRepositoryProvider implements KettleRepositoryProvider {
  public static final String REGION = "pdi-repository-cache";
  private static final Logger LOGGER = LoggerFactory.getLogger( PentahoSessionHolderRepositoryProvider.class );
  private final Supplier<IPentahoSession> pentahoSessionSupplier;
  private final Function<IPentahoSession, ICacheManager> cacheManagerFunction;

  public PentahoSessionHolderRepositoryProvider() {
    this( PentahoSessionHolder::getSession, PentahoSystem::getCacheManager );
  }

  public PentahoSessionHolderRepositoryProvider( Supplier<IPentahoSession> pentahoSessionSupplier,
                                                 Function<IPentahoSession, ICacheManager> cacheManagerFunction ) {
    this.pentahoSessionSupplier = pentahoSessionSupplier;
    this.cacheManagerFunction = cacheManagerFunction;
  }

  @Override public Repository getRepository() {
    IPentahoSession session = pentahoSessionSupplier.get();
    if ( session == null ) {
      LOGGER.debug( "No active Pentaho Session, attempting to load PDI repository unauthenticated." );
      return null;
    }
    ICacheManager cacheManager = cacheManagerFunction.apply( session );

    String sessionName = session.getName();
    Repository repository = (Repository) cacheManager.getFromRegionCache( REGION, sessionName );
    if ( repository == null ) {
      LOGGER.debug( "Repository not cached for user: " + sessionName + "." );
      return null;
    }
    return repository;
  }
}
