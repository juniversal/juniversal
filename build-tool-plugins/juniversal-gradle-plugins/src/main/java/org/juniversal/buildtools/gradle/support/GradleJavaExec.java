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

package org.juniversal.buildtools.gradle.support;

import org.gradle.api.Project;
import org.gradle.api.tasks.JavaExec;
import org.juniversal.buildtools.common.support.CommonJavaExec;

import java.io.File;

/**
 * Created by Bret Johnson on 11/11/2014.
 */
public class GradleJavaExec extends CommonJavaExec {
    private JavaExec javaExec;

    public GradleJavaExec(Project project, String name, File executableJar) {
        javaExec = project.getTasks().create(name, JavaExec.class);

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
