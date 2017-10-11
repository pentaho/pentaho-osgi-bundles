/*!
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
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package org.pentaho.platform.pdi.vfs;

import java.io.InputStream;

import junit.framework.Assert;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.Test;
import org.pentaho.platform.pdi.vfs.MetadataToMondrianVfs;

@SuppressWarnings("nls")
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
    Assert.assertTrue(results.indexOf("<Cube name=\"customer2 Table\">") >= 0);
  }
}
