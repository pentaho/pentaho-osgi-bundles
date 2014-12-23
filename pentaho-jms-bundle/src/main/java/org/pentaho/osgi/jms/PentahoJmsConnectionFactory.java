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
 * Copyright 2014 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.osgi.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import org.apache.activemq.ActiveMQConnectionFactory;

public class PentahoJmsConnectionFactory extends ActiveMQConnectionFactory {
  PentahoJmsConfiguration pentahoJmsConfiguration;
  String userName;
  String password;

  public PentahoJmsConnectionFactory( PentahoJmsConfiguration pentahoJmsConfiguration, String userName, String password ) {
    super();
    this.pentahoJmsConfiguration = pentahoJmsConfiguration;
    this.userName = userName;
    this.password = password;
    
    boolean relay = pentahoJmsConfiguration.getToHost() != null && !pentahoJmsConfiguration.getToHost().isEmpty() ? true : false;
    String jmsBrokerUrl;
    if ( relay ) {
      jmsBrokerUrl = "tcp://" + pentahoJmsConfiguration.getToHost() + ":" + pentahoJmsConfiguration.getPort();
    } else {
      jmsBrokerUrl = "vm://localhost:" + pentahoJmsConfiguration.getPort() + "?create=false&waitForStart=10000";
    }

    super.setUserName( userName );
    super.setPassword( password );
    super.setBrokerURL( jmsBrokerUrl );

  }
  
  public Connection createConnection() throws JMSException {
    return super.createConnection();
  }
  
  public Connection createConnection(String userName, String password) throws JMSException {
    return super.createConnection(userName, password);
  }
}

