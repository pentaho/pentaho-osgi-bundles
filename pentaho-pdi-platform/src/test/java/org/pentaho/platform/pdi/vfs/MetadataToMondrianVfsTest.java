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

package org.pentaho.platform.pdi.vfs;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.Test;

public class MetadataToMondrianVfsTest {
  
  @Test
  public void testVfs() throws Exception {

    // VFS.getManager() is a process-wide singleton, so another test (or a real bundle start()) may have
    // already registered "mtm" on it; removing first makes this registration idempotent regardless of
    // execution order, mirroring what PdiPlatformActivator itself does in production.
    DefaultFileSystemManager fsManager = (DefaultFileSystemManager) VFS.getManager();
    fsManager.removeProvider("mtm");
    fsManager.addProvider("mtm", new MetadataToMondrianVfs());
    
    FileObject fobj = fsManager.resolveFile("mtm:src/test/resources/example_olap.xmi");
    StringBuilder buf = new StringBuilder(1000);
    InputStream in = fobj.getContent().getInputStream();
    int n;
    while ((n = in.read()) != -1) {
        buf.append((char) n);
    }
    in.close();
    String results = buf.toString();
    assertTrue(results.contains("<Cube name=\"customer2 Table\">"));
  }
}
