package org.juniversal

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

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
    File msbuildDirectory;

    /**
     * Path to project or solution .sln file to build.  Defaults to msbuildProject property.
     */
    //@Parameter(property = "projectFile", defaultValue = "${msbuildProject}", required = false)
    File projectFile;

    /**
     * Configuration to build (Release, Debug, etc.).  Defaults to Release.
     */
    //@Parameter(property = "configuration", defaultValue = "Release", required = false)
    String configuration = "Release";

    /**
     * MSBuild output verbosity.  You can specify the following verbosity levels: q[uiet], m[inimal], n[ormal],
     * d[etailed], and diag[nostic].  "normal" is the MSBuild default, but we default to "minimal" instead as that's
     * generally better for batch builds.
     */
    //@Parameter(property = "verbosity", defaultValue = "minimal", required = false)
    String verbosity = "minimal";

    /**
     * Target(s) to build in the project/solution.  Specify each target separately, or use a semicolon or comma to
     * separate multiple targets (e.g., "Resources;Compile".  Defaults to Rebuild.
     */
    //@Parameter(property = "target", defaultValue = "Rebuild", required = false)
    String target = "Rebuild";

    @TaskAction
    public void msbuild() {
        new MSBuildImpl(getLogger(), project.getProjectDir()).msbuild(projectFile, msbuildDirectory, target,
                configuration, verbosity);
    }
}
