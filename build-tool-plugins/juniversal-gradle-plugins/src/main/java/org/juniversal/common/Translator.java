package org.juniversal.common;

import org.juniversal.common.support.FileSet;
import org.juniversal.common.support.CommonProject;
import org.juniversal.common.support.SourceType;

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
