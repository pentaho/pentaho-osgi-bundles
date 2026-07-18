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

package org.pentaho.platform.config;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.url.UrlFileProvider;

import static org.junit.Assert.*;

/**
 * Created by nbaker on 8/7/15.
 */
public class ConfigVfsProviderTest {

  @org.junit.Test
  public void testFindFile() throws Exception {
    DefaultFileSystemManager fileSystemManager = new DefaultFileSystemManager();
    fileSystemManager.addProvider( "pentaho-config", new ConfigVfsProvider() );
    fileSystemManager.addProvider( "file", new UrlFileProvider() );
    fileSystemManager.init();
    FileObject props = fileSystemManager.resolveFile( "pentaho-config:/test.properties" );
    IOUtils.copy( props.getContent().getInputStream(), System.out );
  }
}