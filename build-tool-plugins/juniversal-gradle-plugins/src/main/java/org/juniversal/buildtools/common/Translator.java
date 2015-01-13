/*
 * Copyright (c) 2012-2015, Microsoft Mobile
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

package org.juniversal.buildtools.common;

import org.juniversal.buildtools.common.support.FileSet;
import org.juniversal.buildtools.common.support.CommonProject;
import org.juniversal.buildtools.common.support.SourceType;

import java.io.File;

/**
 * Created by Bret Johnson on 11/15/2014.
 */
public abstract class Translator {
    private CommonProject project;
    private boolean skip;
    private File outputDirectory;
    private File testOutputDirectory;

    public Translator(CommonProject project) {
        this.project = project;
    }

    public CommonProject getProject() {
        return project;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public File getTestOutputDirectory() {
        return testOutputDirectory;
    }

    public void setTestOutputDirectory(File testOutputDirectory) {
        this.testOutputDirectory = testOutputDirectory;
    }

    protected abstract void translateSourceType(SourceType sourceType, File outputDirectory);

    public void translate() {
        if (skip)
            return;

        translateSourceType(SourceType.MAIN, getOutputDirectory());

        FileSet testSourceDirectories = project.getTranslatableSourceDirectories(SourceType.TEST);
        if (! testSourceDirectories.isEmpty()) {
            translateSourceType(SourceType.TEST, testOutputDirectory);
        }
    }
}
