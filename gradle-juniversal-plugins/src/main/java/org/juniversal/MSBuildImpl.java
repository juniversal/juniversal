package org.juniversal;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * @author Bret Johnson
 * @since 7/10/2014 1:56 AM
 */
public class MSBuildImpl {
    private Logger logger;
    private File projectDirectory;

    public MSBuildImpl(Logger logger, File projectDirectory) {
        this.logger = logger;
        this.projectDirectory = projectDirectory;
    }

    public void msbuild(@Nullable File projectFile, @Nullable File msbuildDirectory, String target, String configuration, String verbosity) {
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
            Process msbuild = Utils.exec(args, null, logger);

            BufferedReader reader = new BufferedReader(new InputStreamReader(msbuild.getInputStream()));
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                if (inputLine.contains(": error "))
                    logger.error(inputLine);
                else if (inputLine.contains(": warning "))
                    logger.warn(inputLine);
                else logger.info(inputLine);
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
