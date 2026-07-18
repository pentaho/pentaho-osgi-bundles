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


package org.pentaho.osgi.api;

/**
 * Created by bryan on 4/22/15.
 */
public interface CipherEncryptionService {
  String encrypt( String clearPassword ) throws PasswordServiceException;
  String decrypt( String encryptedPassword ) throws PasswordServiceException;
}
