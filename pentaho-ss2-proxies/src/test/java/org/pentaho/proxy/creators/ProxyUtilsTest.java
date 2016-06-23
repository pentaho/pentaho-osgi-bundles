package org.pentaho.proxy.creators;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.proxy.api.IProxyCreator;
import org.pentaho.platform.proxy.api.IProxyFactory;
import org.pentaho.proxy.creators.securitycontext.SecurityContextProxyCreator;


/**
 * Unit test for ProxyUtils.
 */
public class ProxyUtilsTest {
    SecurityContextProxyCreator mockSecurityContextCreator;

    @Test
    public void testConstructor() {
        ProxyUtils testProxy = ProxyUtils.getInstance();

        Assert.assertNotNull( testProxy );
    }

    @Test
    public void testGetProxyFactory() {
        ProxyUtils testProxy = ProxyUtils.getInstance();

        IProxyFactory factory = testProxy.getProxyFactory();

        Assert.assertNull( factory );
    }

    @Test
    public void testSupportedClass() {
        ProxyUtils testProxy = ProxyUtils.getInstance();

        Assert.assertTrue( ProxyUtils.isRecursivelySupported( "org.pentaho.proxy.creators.ProxyUtils", testProxy.getClass() ) );
        Assert.assertFalse( ProxyUtils.isRecursivelySupported( "org.pentaho.proxy.creators.ProxyUtils2", testProxy.getClass() ) );
        Assert.assertFalse( ProxyUtils.isRecursivelySupported( null, testProxy.getClass() ) );
        Assert.assertFalse( ProxyUtils.isRecursivelySupported( "", testProxy.getClass() ) );
        Assert.assertFalse( ProxyUtils.isRecursivelySupported( null, null ) );
        Assert.assertFalse( ProxyUtils.isRecursivelySupported( "", null ) );
        Assert.assertFalse( ProxyUtils.isRecursivelySupported( "org.pentaho.proxy.creators.securitycontext.ProxyUtils", null ) );
    }
}
