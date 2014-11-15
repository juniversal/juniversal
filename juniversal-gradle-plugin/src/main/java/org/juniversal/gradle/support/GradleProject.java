package org.juniversal.gradle.support;

import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.tasks.SourceSet;
import org.juniversal.common.support.CommonFileSet;
import org.juniversal.common.support.CommonJavaExec;
import org.juniversal.common.support.CommonProject;

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
    public CommonJavaExec createJavaExec(String name, String description, File executableJar) {
        return new GradleJavaExec(project, name, description, executableJar);
    }

    public CommonFileSet createFileSet(FileCollection... fileCollections) {
        CommonFileSet fileSet = new CommonFileSet();
        for (FileCollection fileCollection : fileCollections) {
            fileSet.add(project.files(fileCollection).getFiles());
        }
        return fileSet;
    }

    public CommonFileSet createFileSet(Collection<File> fileCollection) {
        CommonFileSet fileSet = new CommonFileSet();
        fileSet.add(project.files(fileCollection).getFiles());
        return fileSet;
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

    public CommonFileSet getTranslatableSourceFiles(SourceSet sourceSet) {
        // Get all the Java source files, from the source directory(ies) defined in the source set.  Skip source
        // directories whose name ends with "-nontranslated"
        final CommonFileSet sourceFiles = new CommonFileSet();
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
}
