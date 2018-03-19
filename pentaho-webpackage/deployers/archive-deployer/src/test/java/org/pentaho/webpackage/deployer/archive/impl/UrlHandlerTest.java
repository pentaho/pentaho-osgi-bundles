package org.pentaho.webpackage.deployer.archive.impl;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UrlHandlerTest {

  private URL createMockUrlConnection( String payload ) {
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
      URL url = new URL( "http", "someurl.com", 9999, "", stubUrlHandler );

      URL url2 = new URL( "http://someurl.com:9999/index.html" );

      return new URL( "http", "someurl.com", 9999, "", stubUrlHandler );
    } catch ( MalformedURLException e ) {
      e.printStackTrace();
    }
    return null;
  }

  @Test
  public void UrlHandlerTest() {
    // arrange
    URL mockUrl = this.createMockUrlConnection( "empty" );
    mockUrl.getPath();

    // act
    try {
      URLConnection connection = new UrlHandler().openConnection( mockUrl );
    } catch ( IOException e ) {
      e.printStackTrace();
    }

    // assert

  }
}