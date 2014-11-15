package org.juniversal.common.support;

import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Bret Johnson
 * @since 7/8/2014 9:39 PM
 */
public class Utils {
    public static File getFirstThatExists(String messageIfNoneExist, File... files) {
        for (File file : files) {
            if (file.exists())
                return file;
        }

        throw new RuntimeException(messageIfNoneExist);
    }

    public static void deleteChildDirectoriesExcept(File path, String... directoriesToSkip) {
        if (path.isDirectory()) {
            for (File child : path.listFiles()) {
                String name = child.getName();

                if (child.isDirectory()) {
                    boolean skip = false;
                    for (String directoryToSkip : directoriesToSkip) {
                        if (name.equalsIgnoreCase(directoryToSkip)) {
                            skip = true;
                            break;
                        }
                    }

                    if (!skip)
                        deleteRecursively(child);
                }
            }
        }
    }

    /**
     * Delete the specified directory/file.  If there's an error deleting anything, an exception is thrown.
     *
     * @param path directory/file to delete
     */
    public static void deleteRecursively(File path) {
        if (path.isDirectory()) {
            for (File child : path.listFiles())
                deleteRecursively(child);
        }

        if (!path.delete())
            throw new RuntimeException("Failed to delete this file/directory: " + path.getAbsolutePath());
    }

    /**
     * Turn zero or more files into a delimited path string (using ; on Windows and : on Unix platforms), using the
     * syntax like that used to specify Java classpath.
     *
     * @param files vararg parameters for each file to include in the path
     * @return path string; if 0 files are passed, the empty string is returned
     */
    public static String filesToDelimitedPath(File... files) {
        StringBuilder path = new StringBuilder();

        for (File file : files) {
            if (path.length() > 0)
                path.append(File.pathSeparator);
            path.append(file.getAbsolutePath());
        }

        return path.toString();
    }
}
