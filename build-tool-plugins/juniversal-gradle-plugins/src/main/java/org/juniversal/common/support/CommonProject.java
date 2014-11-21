package org.juniversal.common.support;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Bret Johnson on 11/11/2014.
 */
public abstract class CommonProject {
    public abstract CommonJavaExec createJavaExec(String name, File executableJar);

    /**
     * Get the project's root directory.
     *
     * @return project root directory
     */
    public abstract File getProjectDirectory();

    /**
     * Log a debug message, as appropriate for the build system.
     *
     * @param message message to log
     */
    public abstract void debug(String message);

    /**
     * Log an info message, as appropriate for the build system.
     *
     * @param message message to log
     */
    public abstract void info(String message);

    /**
     * Log a warning message, as appropriate for the build system.
     *
     * @param message message to log
     */
    public abstract void warn(String message);

    /**
     * Log an error message, as appropriate for the build system.
     *
     * @param message message to log
     */
    public abstract void error(String message);

    public abstract FileSet getTranslatableSourceDirectories(SourceType sourceType);

    public abstract FileSet getAllSourceDirectories(SourceType sourceType);

    public abstract FileSet getTranslatableSourceFiles(SourceType sourceType);

    public abstract FileSet getClasspath(SourceType sourceType);

    /**
     * Execute the specified shell command + arguments.
     *
     * @param args      command to execute, with arguments
     * @param directory working directory when running the command, or null to use current directory
     * @return project object
     */
    public Process exec(List<String> args, @Nullable File directory) {
        StringBuilder fullCommandLine = new StringBuilder();
        for (String arg : args)
            fullCommandLine.append(arg + " ");
        debug("Executing: " + fullCommandLine);

        String[] argsArray = args.toArray(new String[0]);
        Process process;
        try {
            process = Runtime.getRuntime().exec(argsArray, null, directory);
        } catch (IOException e) {
            throw new RuntimeException("IO exception when running: " + fullCommandLine, e);
        }

        return process;
    }
}
