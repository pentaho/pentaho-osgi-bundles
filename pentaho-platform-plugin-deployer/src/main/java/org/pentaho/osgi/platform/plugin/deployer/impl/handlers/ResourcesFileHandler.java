package org.pentaho.osgi.platform.plugin.deployer.impl.handlers;

import org.pentaho.osgi.platform.plugin.deployer.api.PluginFileHandler;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginHandlingException;
import org.pentaho.osgi.platform.plugin.deployer.api.PluginMetadata;

import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Pattern;

/**
 * Created by jfigueiredo on 02/04/2017.
 */
public class ResourcesFileHandler implements PluginFileHandler {

    public static final Pattern LIB_PATTERN = Pattern.compile(".+\\/resources\\/.*\\.properties");

    @Override
    public boolean handles(String fileName) {
        return LIB_PATTERN.matcher(fileName).matches();
    }

    @Override
    public boolean handle(String relativePath, byte[] file, PluginMetadata pluginMetadata) throws PluginHandlingException {

        OutputStream outputStream = null;
        try {
            outputStream = pluginMetadata.getFileOutputStream(relativePath.substring(relativePath.indexOf("/resources/") + 1));
            outputStream.write(file);

        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }
}
