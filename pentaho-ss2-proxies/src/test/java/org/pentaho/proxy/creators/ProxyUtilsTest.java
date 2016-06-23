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

        Assert.assertTrue( ProxyUtils.isRecursivelySupported( "org.springframework.security.GrantedAuthority", org.pentaho.proxy.creators.userdetailsservice.ProxyGrantedAuthority.class ) );
        Assert.assertFalse( ProxyUtils.isRecursivelySupported( "org.springframework.security.GrantedAuthorityX", org.pentaho.proxy.creators.userdetailsservice.ProxyGrantedAuthority.class ) );
        Assert.assertTrue( ProxyUtils.isRecursivelySupported( "org.springframework.security.GrantedAuthority", org.springframework.security.GrantedAuthority.class ) );
        Assert.assertTrue( ProxyUtils.isRecursivelySupported( "java.lang.Cloneable", java.lang.Cloneable.class ) );
    }

    @Test
    public void testMethodByName() {
        ProxyUtils testProxy = ProxyUtils.getInstance();

        Assert.assertNull( ProxyUtils.findMethodByName( java.lang.Cloneable.class, "test" ) );
        Assert.assertNotNull( ProxyUtils.findMethodByName( java.lang.String.class, "toString" ) );
    }
}
