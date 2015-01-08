/*
 * Copyright (c) 2012-2014, Microsoft Mobile
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.juniversal.buildtools.common.support;

import java.io.File;

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

    public static String argsToCommandLineString(String[] args) {
        StringBuilder buffer = new StringBuilder();

        for (String arg : args) {
            if (buffer.length() > 0)
                buffer.append(" ");

            if (arg.contains(" "))
                buffer.append("\"" + arg + "\"");
            else buffer.append(arg);
        }

        return buffer.toString();
    }
}
