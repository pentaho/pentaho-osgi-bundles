/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
