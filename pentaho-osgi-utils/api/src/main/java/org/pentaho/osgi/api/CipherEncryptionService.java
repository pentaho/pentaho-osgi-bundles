/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.osgi.api;

/**
 * Created by bryan on 4/22/15.
 */
public interface CipherEncryptionService {
  String encrypt( String clearPassword ) throws PasswordServiceException;
  String decrypt( String encryptedPassword ) throws PasswordServiceException;
}
