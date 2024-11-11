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
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.Test;
import org.pentaho.platform.pdi.vfs.MetadataToMondrianVfs;

public class MetadataToMondrianVfsTest {
  
  @Test
  public void testVfs() throws Exception {
    
    ((DefaultFileSystemManager)VFS.getManager()).addProvider("mtm", new MetadataToMondrianVfs());
    
    FileSystemManager fsManager = VFS.getManager();
    FileObject fobj = fsManager.resolveFile("mtm:src/test/resources/example_olap.xmi");
    StringBuilder buf = new StringBuilder(1000);
    InputStream in = fobj.getContent().getInputStream();
    int n;
    while ((n = in.read()) != -1) {
        buf.append((char) n);
    }
    in.close();
    String results = buf.toString();
    assertTrue(results.indexOf("<Cube name=\"customer2 Table\">") >= 0);
  }
}
