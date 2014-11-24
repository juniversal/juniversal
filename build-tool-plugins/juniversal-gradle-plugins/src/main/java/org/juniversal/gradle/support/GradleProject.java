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

package org.juniversal.gradle.support;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.juniversal.common.support.FileSet;
import org.juniversal.common.support.CommonJavaExec;
import org.juniversal.common.support.CommonProject;
import org.juniversal.common.support.SourceType;

import java.io.File;
import java.util.Collection;

/**
 * Created by Bret Johnson on 11/11/2014.
 */
public class GradleProject extends CommonProject {
    private Project project;

    public GradleProject(Project project) {
        this.project = project;
    }

    @Override
    public File getProjectDirectory() {
        return project.getProjectDir();
    }

    @Override
    public CommonJavaExec createJavaExec(String name, File executableJar) {
        return new GradleJavaExec(project, name, executableJar);
    }

    public FileSet createFileSet(FileCollection... fileCollections) {
        FileSet fileSet = new FileSet();
        for (FileCollection fileCollection : fileCollections) {
            fileSet.add(project.files(fileCollection).getFiles());
        }
        return fileSet;
    }

    public FileSet createFileSet(Collection<File> fileCollection) {
        FileSet fileSet = new FileSet();
        fileSet.add(project.files(fileCollection).getFiles());
        return fileSet;
    }

    @Override
    public FileSet getTranslatableSourceDirectories(SourceType sourceType) {
        FileSet fileSet = createFileSet();

        for (File directory : getSourceSet(sourceType).getJava().getSrcDirs()) {
            if (! directory.toString().contains("nontranslated"))
                fileSet.add(directory);
        }

        return fileSet;
    }

    @Override
    public FileSet getAllSourceDirectories(SourceType sourceType) {
        return createFileSet(getSourceSet(sourceType).getAllJava().getSrcDirs());
    }

    @Override
    public FileSet getTranslatableSourceFiles(SourceType sourceType) {
        SourceSet sourceSet = getSourceSet(sourceType);

        // Get all the Java source files, from the source directory(ies) defined in the source set.  Skip source
        // directories whose name ends with "-nontranslated"
        final FileSet sourceFiles = new FileSet();
        sourceSet.getJava().visit(new FileVisitor() {
            @Override
            public void visitDir(FileVisitDetails fileVisitDetails) {
                File parentFile = fileVisitDetails.getFile().getParentFile();

                if (parentFile.getName().endsWith("-nontranslated"))
                    fileVisitDetails.stopVisiting();
            }

            @Override
            public void visitFile(FileVisitDetails fileVisitDetails) {
                File parentFile = fileVisitDetails.getFile().getParentFile();
                if (parentFile.getName().endsWith("-nontranslated"))
                    fileVisitDetails.stopVisiting();
                else sourceFiles.add(fileVisitDetails.getFile());
            }
        });
        return sourceFiles;
    }

    @Override
    public FileSet getClasspath(SourceType sourceType) {
        return createFileSet(getSourceSet(sourceType).getCompileClasspath());
    }

    public SourceSet getSourceSet(SourceType sourceType) {
        JavaPluginConvention javaPluginConvention;
        try {
            javaPluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
        } catch (IllegalStateException e) {
            throw new RuntimeException("Gradle project apparently isn't a Java project--it doesn't use the Java plugin");
        }

        SourceSetContainer sourceSets = javaPluginConvention.getSourceSets();
        return sourceSets.getByName(getSourceSetName(sourceType));
    }

    public String getSourceSetName(SourceType sourceType) {
        if (sourceType == SourceType.TEST)
            return SourceSet.TEST_SOURCE_SET_NAME;
        else return SourceSet.MAIN_SOURCE_SET_NAME;
    }

    @Override
    public void debug(String message) {
        project.getLogger().debug(message);
    }

    @Override
    public void info(String message) {
        project.getLogger().info(message);
    }

    @Override
    public void warn(String message) {
        project.getLogger().warn(message);
    }

    @Override
    public void error(String message) {
        project.getLogger().error(message);
    }
}
