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
package org.pentaho.osgi.impl;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CipherEncryptionServiceImplTest {

  @Test
  public void testCipherDefaultValues() {
    CipherEncryptionServiceImpl service =
      new CipherEncryptionServiceImpl( "_CyPh3r_", "PBEWithMD5AndDES", "P3ntah0C1ph3r", 19 );
    assertNotNull( service );
  }

  @Test
  public void testCipherEncryptionWithDefaults() throws Exception {
    CipherEncryptionServiceImpl service =
      new CipherEncryptionServiceImpl( "_CyPh3r_", "PBEWithMD5AndDES", "P3ntah0C1ph3r", 19 );
    // Everything should be set up now for encryption testing
    String stringToEncrypt = "String To Encrypt";
    String encryptedStringExpected = "R1pxG/vXQU8ezFM5VE644dqQxCKNP+Ap";
    String encryptedStringActual = service.encrypt( stringToEncrypt );
    Assert.assertEquals( encryptedStringExpected, encryptedStringActual );
  }

  @Test
  public void testCipherDecryptionWithDefaults() throws Exception {
    CipherEncryptionServiceImpl service =
      new CipherEncryptionServiceImpl( "_CyPh3r_", "PBEWithMD5AndDES", "P3ntah0C1ph3r", 19 );
    // Everything should be set up now for encryption testing
    String encryptedString = "R1pxG/vXQU8ezFM5VE644dqQxCKNP+Ap";
    String decryptedStringExpected = "String To Encrypt";
    String decryptedStringActual = service.decrypt( encryptedString );
    Assert.assertEquals( decryptedStringExpected, decryptedStringActual );
  }

  @Test
  public void testSaltEffectOnEncryption() throws Exception {
    CipherEncryptionServiceImpl service =
      new CipherEncryptionServiceImpl( "DiffSalt", "PBEWithMD5AndDES", "P3ntah0C1ph3r", 19 );
    // Everything should be set up now for encryption testing
    String encryptedStringShouldNotBe = "R1pxG/vXQU8ezFM5VE644dqQxCKNP+Ap";
    String stringToEncrypt = "String To Encrypt";
    String encryptedStringActual = service.encrypt( stringToEncrypt );
    Assert.assertNotSame( encryptedStringShouldNotBe, encryptedStringActual );
  }

  @Test( expected = RuntimeException.class )
  public void testSaltTooLong() throws Exception {
    CipherEncryptionServiceImpl service =
      new CipherEncryptionServiceImpl( "This Salt Is Too Long And Will Be Truncated", "PBEWithMD5AndDES",
        "P3ntah0C1ph3r", 19 );
    assertNotNull( service );
  }

  @Test( expected = RuntimeException.class )
  public void testSaltTooShort() throws Exception {
    CipherEncryptionServiceImpl service =
      new CipherEncryptionServiceImpl( "short", "PBEWithMD5AndDES", "P3ntah0C1ph3r", 19 );
    assertNotNull( service );
  }

  @Test
  public void testIterationsEffectOnEncryption() throws Exception {
    CipherEncryptionServiceImpl service =
      new CipherEncryptionServiceImpl( "_CyPh3r_", "PBEWithMD5AndDES", "P3ntah0C1ph3r", 25 );
    // Everything should be set up now for encryption testing
    String encryptedStringShouldNotBe = "R1pxG/vXQU8ezFM5VE644dqQxCKNP+Ap";
    String stringToEncrypt = "String To Encrypt";
    String encryptedStringActual = service.encrypt( stringToEncrypt );
    Assert.assertNotSame( encryptedStringShouldNotBe, encryptedStringActual );
  }

  @Test
  public void testThreadSafetyOfCipherService() throws Exception {
    int threadCount = 30;
    final CipherEncryptionServiceImpl service =
      new CipherEncryptionServiceImpl( "_CyPh3r_", "PBEWithMD5AndDES", "P3ntah0C1ph3r", 19 );
    ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>( threadCount );
    ThreadPoolExecutor executor = new ThreadPoolExecutor( 10, 50, 1, TimeUnit.SECONDS, queue );

    for ( int i = 0; i < threadCount; i++ ) {
      executor.execute( new Runnable() {
        @Override
        public void run() {
          try {
            String s = null;
            String enc = null;
            String dec = null;
            for ( int i = 0; i < 50; i++ ) {
              s = UUID.randomUUID().toString();
              enc = service.encrypt( s );
              dec = service.decrypt( enc );
              Assert.assertEquals( s, dec );
            }
          } catch ( Exception ex ) {
            ex.printStackTrace();
            Assert.fail( ex.toString() );
          }
        }
      } );
    }
    executor.shutdown();
    boolean isTerminated = executor.awaitTermination( 200, TimeUnit.SECONDS );
    if ( !isTerminated ) {
      Assert.fail( "It took too long to run the threading test." );
    }

  }
}
