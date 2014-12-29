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

package org.juniversal.common.support;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Bret Johnson on 11/12/2014.
 */
public class FileSet {
    private ArrayList<File> files = new ArrayList<File>();

    public FileSet() {
    }

    public FileSet(FileSet other) {
        add(other.getFiles());
    }

    public FileSet(Collection<File> files) {
        add(files);
    }

    public void add(File... files) {
        for (File file : files) {
            this.files.add(file);
        }
    }

    public void add(Collection<File> files) {
        this.files.addAll(files);
    }

    public List<File> getFiles() {
        return files;
    }

    public boolean isEmpty() {
        return files.isEmpty();
    }

    public File getSingleElement() {
        if (files.size() != 1)
            throw new RuntimeException("FileSet unexpectedly has " + files.size() +
                                       " elements, when it should have exactly 1");
        return files.get(0);
    }

    /**
     * Get all the files (normally directories in this case) in this file set as a path string, using the platform's
     * appropriate path delimiter.
     *
     * @return path string, listing all files/directories in this file set
     */
    public String getAsPath() {
        StringBuilder path = new StringBuilder();

        for (File file : files) {
            if (path.length() > 0)
                path.append(File.pathSeparator);
            path.append(file.getPath());
        }

        return path.toString();
    }

    /**
     * Get all the files (normally directories in this case) in this file set as a path string, using the platform's
     * appropriate path delimiter, skipping but warning on any that don't exist.
     *
     * @param project     project (for logging warnings)
     * @param fileSetType what kind of entries these are, used to prefix warning messages
     * @return path string, listing all files/directories in this file set
     */
    public String getAsPathEnsureExists(CommonProject project, String fileSetType) {
        StringBuilder path = new StringBuilder();

        for (File file : files) {
            if (!file.exists()) {
                project.warn(fileSetType + " path entry does not exist: " + file.toString());
                continue;
            }

            if (path.length() > 0)
                path.append(File.pathSeparator);
            path.append(file.getPath());
        }

        return path.toString();
    }
}
