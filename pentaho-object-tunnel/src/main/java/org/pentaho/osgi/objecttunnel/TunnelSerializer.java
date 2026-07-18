/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.osgi.objecttunnel;


import java.util.List;

/**
 * Instances support marshalling types to and from String.
 *
 * Created by nbaker on 2/14/17.
 */
public interface TunnelSerializer<T> {
  List<Class> getSupportedClasses();

  String serialize( Object object );
  T deserialize( String serializedString );
}
