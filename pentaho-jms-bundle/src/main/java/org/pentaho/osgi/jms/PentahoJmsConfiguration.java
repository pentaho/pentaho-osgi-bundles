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

public class PentahoJmsConfiguration {
  private String toHost; // the host to relay the jms messages to, or leave empty to use localhost
  private int port = -1; // The port for jms messages
  private boolean enabled; // Whether jms is enabled or disabled
  private String userName;
  private String password;
  
  public PentahoJmsConfiguration( boolean enabled, int port, String toHost, String userName, String password ){
    this.enabled = enabled;
    this.port = port;
    this.toHost = toHost;
    this.userName = userName;
    this.password = password;
  }

  public String getToHost() {
    return toHost;
  }

  public int getPort() {
    return port;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public String getUserName() {
    return userName;
  }

  public String getPassword() {
    return password;
  }
  
  public boolean isNotRelayServer() {
    return !isRelayServer();
  }
  
  public boolean isRelayServer() {
    return toHost != null && !toHost.isEmpty() ? true : false;
  }
  
}
