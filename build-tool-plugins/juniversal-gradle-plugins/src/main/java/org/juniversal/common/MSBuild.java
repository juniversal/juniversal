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

package org.juniversal.common;

import org.jetbrains.annotations.Nullable;
import org.juniversal.common.support.CommonProject;
import org.juniversal.common.support.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * @author Bret Johnson
 * @since 7/10/2014 1:56 AM
 */
public class MSBuild {
    private CommonProject project;

    public MSBuild(CommonProject project) {
        this.project = project;
    }

    public void build(@Nullable File projectFile, @Nullable File msbuildDirectory, String target, String configuration, String verbosity) {
        File projectDirectory = project.getProjectDirectory();

        File effectiveProjectFile;
        if (projectFile != null) {
            effectiveProjectFile = projectFile;
            if (!effectiveProjectFile.exists())
                throw new RuntimeException("Specified projectFile " + effectiveProjectFile + " does not exist");
        } else {
            File projectCsharpDirectory = new File(projectDirectory, "c#");
            String projectFileName = projectDirectory.getName() + ".csproj";

            effectiveProjectFile = Utils.getFirstThatExists(
                    "projectFile not specified and default project file name " + projectFileName + " not found in default locations of <project-dir> or <project-dir>/c#",
                    new File(projectDirectory, projectFileName),
                    new File(projectCsharpDirectory, projectFileName));
        }

        File msbuildPath;
        if (msbuildDirectory != null)
            msbuildPath = new File(msbuildDirectory, "MSBuild.exe");
        else {
            msbuildPath = Utils.getFirstThatExists(
                    "msbuildDirectory not specified and MSBuild.exe not found in default locations",
                    new File("C:\\Program Files\\MSBuild\\12.0\\bin\\MSBuild.exe"),
                    new File("C:\\Program Files (x86)\\MSBuild\\12.0\\bin\\MSBuild.exe"));
        }

        ArrayList<String> args = new ArrayList<String>();
        args.add(msbuildPath.getPath());
        args.add("/t:" + target);
        args.add("/property:Configuration=" + configuration);
        args.add("/verbosity:" + verbosity);
        args.add(effectiveProjectFile.getAbsolutePath());

        try {
            Process msbuild = project.exec(args, null);

            BufferedReader reader = new BufferedReader(new InputStreamReader(msbuild.getInputStream()));
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                if (inputLine.contains(": error "))
                    project.error(inputLine);
                else if (inputLine.contains(": warning "))
                    project.warn(inputLine);
                else project.info(inputLine);
            }
            reader.close();

            int exitCode = msbuild.exitValue();
            if (exitCode > 0)
                throw new RuntimeException("MSBuild failed with exit code " + exitCode);
        } catch (IOException e) {
            throw new RuntimeException("IO exception when running MSBuild", e);
        }
    }
}
