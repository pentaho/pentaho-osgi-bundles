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
 * Copyright 2016 Pentaho Corporation. All rights reserved.
 */

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
