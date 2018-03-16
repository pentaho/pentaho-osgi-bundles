package org.pentaho.webpackage.core.impl;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.pentaho.webpackage.core.IPentahoWebPackage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestUtils {

  static URL createMockUrlConnection( String payload ) {
    URLConnection mockUrlCon = mock( URLConnection.class );
    URLStreamHandler stubUrlHandler = null;
    try {
      stubUrlHandler = new URLStreamHandler() {
        @Override
        protected URLConnection openConnection( URL u ) throws IOException {
          return mockUrlCon;
        }
      };
      when( mockUrlCon.getInputStream() ).thenReturn( new ByteArrayInputStream( payload.getBytes() ) );
    } catch ( IOException ignored ) {
    }
    try {
      return new URL( "http", "someurl.com", 9999, "", stubUrlHandler );
    } catch ( MalformedURLException e ) {
      e.printStackTrace();
    }
    return null;
  }

  static URL createInvalidMockUrlConnection( String payload ) {
    URLStreamHandler stubUrlHandler = stubUrlHandler = new URLStreamHandler() {
      @Override
      protected URLConnection openConnection( URL u ) throws IOException {
        throw new IOException( "Can't open connection" );
      }
    };

    try {
      return new URL( "http", "someurl.com", 9999, "", stubUrlHandler );
    } catch ( MalformedURLException e ) {
      e.printStackTrace();
    }
    return null;
  }

  static Bundle createBaseMockBundle( String bundleName, String bundleVersion, int bundleState, ServiceRegistration mockServiceReference ) {
    Bundle mockBundle = mock( Bundle.class );
    when( mockBundle.getSymbolicName() ).thenReturn( bundleName );
    Version version = mock( Version.class );
    when( version.toString() ).thenReturn( bundleVersion );
    when( mockBundle.getVersion() ).thenReturn( version );
    when( mockBundle.getState() ).thenReturn( bundleState );

    BundleContext mockBundleContext = mock( BundleContext.class );
    when( mockBundleContext.getBundle() ).thenReturn( mockBundle );
    when( mockBundle.getBundleContext() ).thenReturn( mockBundleContext );
    when( mockBundleContext.registerService( eq( IPentahoWebPackage.class.getName() ), any(), any() ) )
        .thenReturn( mockServiceReference );

    return mockBundle;

  }
}
