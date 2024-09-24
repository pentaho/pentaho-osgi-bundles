/*!
 * Copyright 2010 - 2022 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.platform.pdi;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.util.ThinModelConverter;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;

/**
 * This class was moved here from the AgielBI Plugin. It takes a path to an XMI file as a datasource name and extracts
 * the DatabaseMeta out of that model.
 */
public class XmiToDatabaseMetaDatasourceService implements IDBDatasourceService {

  public void clearCache() {
    // TODO impl cache
  }

  public void clearDataSource( String dsName ) {
    // TODO impl cache
  }

  public String getDSBoundName( String dsName ) throws DBDatasourceServiceException {
    return dsName;
  }

  public String getDSUnboundName( String dsName ) {
    return dsName;
  }

  public DataSource getDataSource( String dsName ) throws DBDatasourceServiceException {
    Domain domain = null;
    try {
      InputStream inStream;
      XmiParser parser = new XmiParser();
      File file = new File( dsName );
      if ( file.exists() ) {
        inStream = new FileInputStream( file );
      } else {
        inStream = new URL( dsName ).openStream();
      }
      domain = parser.parseXmi( inStream );
    } catch ( Exception e ) {
      throw new DBDatasourceServiceException( e );
    }

    if ( domain.getPhysicalModels().size() == 0 || !( domain.getPhysicalModels().get(
        0 ) instanceof SqlPhysicalModel ) ) {
      throw new DBDatasourceServiceException( "No SQL Physical Model Available" );

    }

    SqlPhysicalModel model = (SqlPhysicalModel) domain.getPhysicalModels().get( 0 );

    DatabaseMeta databaseMeta = ThinModelConverter.convertToLegacy( model.getId(), model.getDatasource() );
    return new DatabaseMetaDataSource( databaseMeta );
  }

  public DataSource resolveDatabaseConnection( IDatabaseConnection databaseConnection ) {
    throw new UnsupportedOperationException( "resolveDatabaseConnection is not implemented." );
  }

  class DatabaseMetaDataSource implements DataSource {

    DatabaseMeta databaseMeta;

    public DatabaseMetaDataSource( DatabaseMeta databaseMeta ) {
      this.databaseMeta = databaseMeta;
    }

    public Connection getConnection() throws SQLException {
      Database database = new Database( databaseMeta );
      try {
        database.connect();
      } catch ( KettleException e ) {
        e.printStackTrace();
        throw new SQLException( e.getMessage() );
      }
      return database.getConnection();
    }

    public Connection getConnection( String username, String password ) throws SQLException {
      return null;
    }

    public PrintWriter getLogWriter() throws SQLException {
      return null;
    }

    public int getLoginTimeout() throws SQLException {
      return 0;
    }

    public void setLogWriter( PrintWriter out ) throws SQLException {
    }

    public void setLoginTimeout( int seconds ) throws SQLException {
    }

    public boolean isWrapperFor( Class<?> iface ) {
      return false;
    }

    public <T> T unwrap( Class<T> iface ) {
      return null;
    }

    public Logger getParentLogger() {
      return null;
    }

  }

}
