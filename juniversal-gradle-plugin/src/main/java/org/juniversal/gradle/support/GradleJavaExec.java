package org.juniversal.gradle.support;

import org.gradle.api.Project;
import org.gradle.api.tasks.JavaExec;
import org.juniversal.common.support.CommonJavaExec;

import java.io.File;

/**
 * Created by Bret Johnson on 11/11/2014.
 */
public class GradleJavaExec extends CommonJavaExec {
    private JavaExec javaExec;

    public GradleJavaExec(Project project, String name, String description, File executableJar) {
        javaExec = project.getTasks().create(name, JavaExec.class);
        javaExec.setDescription(description);

        javaExec.setWorkingDir(project.getProjectDir());

        // Because JavaExec doesn't have proper support for exec'ing runnable jars, use this hack,
        // saying the main class is "-jar", to make it work
        javaExec.setMain("-jar");
        javaExec.args(executableJar.getAbsolutePath());
    }

    @Override public void addArg(String... args) {
        for (String arg : args) {
            javaExec.args(arg);
        }
    }

    @Override public String getCommandLine() {
        return javaExec.getCommandLine().toString();
    }

    @Override public void exec() {
        javaExec.exec();
    }
}
