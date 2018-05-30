package com.redhat.PatrIoT.network_demo.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;

public class FileUtils {
    public String convertToFile(InputStream inputStream, String name) {
        File targetFile = new File(name);
        try {
            java.nio.file.Files.copy(
                    inputStream,
                    targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
            org.apache.commons.compress.utils.IOUtils.closeQuietly(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return targetFile.getAbsolutePath();
    }
}
