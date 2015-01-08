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

package org.juniversal.buildtools.common;

import org.juniversal.buildtools.common.support.*;
import org.juniversal.buildtools.common.support.FileSet;

import java.io.File;

/**
 * @author Bret Johnson
 * @since 7/10/2014 1:56 AM
 */
public class JavaToObjectiveC extends Translator {
    private File j2objcHome;
    private boolean useArc;
    private boolean docComments;

    public JavaToObjectiveC(CommonProject project) {
        super(project);
    }

    public void setJ2objcHome(File j2objcHome) {
        this.j2objcHome = j2objcHome;
    }

    public void setUseArc(boolean useArc) {
        this.useArc = useArc;
    }

    public void setDocComments(boolean docComments) {
        this.docComments = docComments;
    }

    @Override
    public void translateSourceType(SourceType sourceType, File outputDirectory) {
        FileSet classpath = getProject().getClasspath(sourceType);
        FileSet sourcepath = getProject().getAllSourceDirectories(sourceType);
        FileSet translatableSourceFiles = getProject().getTranslatableSourceFiles(sourceType);

        // Delete the generated source directories
        Utils.deleteChildDirectoriesExcept(outputDirectory, "nontranslated");
        
        File j2objcLibDirectory = new File(j2objcHome, "lib");

        FileSet j2objcClasspath = new FileSet(classpath);
        j2objcClasspath.add(new File(j2objcLibDirectory, "j2objc_annotations.jar"));

        String taskName = sourceType == SourceType.MAIN ? "translateJavaToObjectiveC" : "translateJavaTestToObjectiveC";

        CommonJavaExec javaExec = getProject().createJavaExec(taskName, new File(j2objcLibDirectory, "j2objc.jar"));
        //javaExec.setDescription("Converts Java to Objective C");

        //j2objc.setGroup(DOCUMENTATION_GROUP);
        //j2objc.setClasspath(mainSourceSet.getOutput().plus(mainSourceSet.getCompileClasspath()));
        //j2objc.setSource(mainSourceSet.getAllJava());
        //addDependsOnTaskInOtherProjects(j2objc, true, JAVADOC_TASK_NAME, COMPILE_CONFIGURATION_NAME);

        javaExec.addArg("-Xbootclasspath:" + new File(j2objcLibDirectory, "jre_emul.jar").getPath());

        javaExec.addArg("-classpath", j2objcClasspath.getAsPath());

        if (!sourcepath.isEmpty())
            javaExec.addArg("-sourcepath", sourcepath.getAsPath());

        javaExec.addArg("-d", outputDirectory.getPath());

        if (useArc)
            javaExec.addArg("-use-arc");
        if (docComments)
            javaExec.addArg("--doc-comments");

        for (File file : translatableSourceFiles.getFiles()) {
            javaExec.addArg(file.getAbsolutePath());
        }

        getProject().info(javaExec.getCommandLine());
        javaExec.exec();
    }

/*
    private JavaExec createJ2objcTask(JavaPluginConvention javaPluginConvention, boolean forTest) {
        Project project = getProject();
        SourceSetContainer sourceSets = javaPluginConvention.getSourceSets();
        SourceSet sourceSet = sourceSets.getByName(forTest ? SourceSet.TEST_SOURCE_SET_NAME : SourceSet.MAIN_SOURCE_SET_NAME);

        File objCOutputDirectory = forTest ? testOutputDirectory : outputDirectory;

        File j2objcLibDirectory = new File(j2objcHome, "lib");

        ConfigurableFileCollection sourcePath;
        if (!forTest)
            sourcePath = getProject().files(sourceSet.getAllJava().getSrcDirs());
        else {
            SourceSet mainSourceSet = sourceSets.getByName("main");
            sourcePath = getProject().files(sourceSet.getAllJava().getSrcDirs(), mainSourceSet.getAllJava().getSrcDirs());
        }

        // Get all the Java source files, from the source directory set defined for Java.   Skip source directories
        // whose name ends with "-nontranslated"
        final ArrayList<File> sourceFiles = new ArrayList<File>();
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

        ConfigurableFileCollection classpath = getProject().files(
                new File(j2objcLibDirectory, "j2objc_annotations.jar"), sourceSet.getCompileClasspath());

        JavaExec javaExec = project.getTasks().create(forTest ? "j2objc-test" : "j2objc-main", JavaExec.class);
        javaExec.setDescription("Converts Java to Objective C");
        //j2objc.setGroup(DOCUMENTATION_GROUP);
        //j2objc.setClasspath(mainSourceSet.getOutput().plus(mainSourceSet.getCompileClasspath()));
        //j2objc.setSource(mainSourceSet.getAllJava());
        //addDependsOnTaskInOtherProjects(j2objc, true, JAVADOC_TASK_NAME, COMPILE_CONFIGURATION_NAME);

        javaExec.setWorkingDir(project.getProjectDir());

        // Because JavaExec doesn't have proper support for exec'ing runnable jars, use this hack,
        // saying the main class is "-jar", to make it work
        javaExec.setMain("-jar");
        javaExec.args(new File(j2objcLibDirectory, "j2objc.jar").getAbsolutePath());

        javaExec.args("-Xbootclasspath:" + project.files(new File(j2objcLibDirectory, "jre_emul.jar")).getAsPath());

        javaExec.args("-classpath", classpath.getAsPath());

        if (!sourcePath.isEmpty())
            javaExec.args("-sourcepath", sourcePath.getAsPath());

        javaExec.args("-d", objCOutputDirectory.getPath());

        if (useArc)
            javaExec.args("-use-arc");
        if (docComments)
            javaExec.args("--doc-comments");

        for (File file : sourceFiles) {
            javaExec.args(file.getAbsolutePath());
        }

        Logger myLogger = getLogger();
        myLogger.info(javaExec.getCommandLine().toString());

        return javaExec;
    }
*/
}

