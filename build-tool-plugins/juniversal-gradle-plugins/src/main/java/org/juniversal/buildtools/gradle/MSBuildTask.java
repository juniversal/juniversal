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

package org.juniversal.buildtools.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.juniversal.buildtools.common.MSBuild;
import org.juniversal.buildtools.gradle.support.GradleProject;

import java.io.File;

/**
 * @author Bret Johnson
 * @since 7/8/2014 7:53 PM
 */
public class MSBuildTask extends DefaultTask {
    /**
     * Path to the directory where MSBuild is installed.  This directory should contain MSBuild.exe.  Defaults to the
     * value of the msbuildDirectory property.
     */
    //@Parameter(property = "msbuildDirectory", defaultValue = "${msbuildDirectory}", required = false)
    public File msbuildDirectory;

    /**
     * Path to project or solution .sln file to build.  Defaults to msbuildProject property.
     */
    //@Parameter(property = "projectFile", defaultValue = "${msbuildProject}", required = false)
    public File projectFile;

    /**
     * Configuration to build (Release, Debug, etc.).  Defaults to Release.
     */
    //@Parameter(property = "configuration", defaultValue = "Release", required = false)
    public String configuration = "Release";

    /**
     * MSBuild output verbosity.  You can specify the following verbosity levels: q[uiet], m[inimal], n[ormal],
     * d[etailed], and diag[nostic].  "normal" is the MSBuild default, but we default to "minimal" instead as that's
     * generally better for batch builds.
     */
    //@Parameter(property = "verbosity", defaultValue = "minimal", required = false)
    public String verbosity = "minimal";

    /**
     * Target(s) to build in the project/solution.  Specify each target separately, or use a semicolon or comma to
     * separate multiple targets (e.g., "Resources;Compile".  Defaults to Rebuild.
     */
    //@Parameter(property = "target", defaultValue = "Rebuild", required = false)
    public String target = "Rebuild";


    @TaskAction
    public void msbuild() {
        MSBuild msBuild = new MSBuild(new GradleProject(getProject()));
        msBuild.build(projectFile, msbuildDirectory, target, configuration, verbosity);
    }

}
