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
}
