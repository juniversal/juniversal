package org.juniversal

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public class JavaToCSharpTask extends DefaultTask {
    /**
     * Java main source directory.  Defaults to ${project.build.sourceDirectory}.
     */
    //@Parameter(defaultValue = "${project.build.sourceDirectory}", property = "sourceDirectory", required = false)
    File sourceDirectory = getProject().file("src/main/java");

    /**
     * Directory for generated C# for main source.  Defaults to ${project.basedir}/c#.
     */
    //@Parameter(defaultValue = "${project.basedir}/c#", property = "outputDirectory", required = false)
    File outputDirectory = getProject().file("c#");

    /**
     * Java test source directory.  Defaults to ${project.build.testSourceDirectory}.  If this directory doesn't exist,
     * then no test sources are converted.
     */
    //@Parameter(defaultValue = "${project.build.testSourceDirectory}", property = "testSourceDirectory", required = false)
    File testSourceDirectory = getProject().file("src/test/java");

    /**
     * Directory for generated C# for test source.  Defaults to ${project.basedir}/c#-test.
     */
    //@Parameter(defaultValue = "${project.basedir}/c#-test", property = "testOutputDirectory", required = false)
    File testOutputDirectory = getProject().file("c#-test");

    /**
     * Directory that contains the converter.  Java to C# Converter.exe must be in this directory.
     */
    //@Parameter(property = "j2objcHome", required = true)
    File converterDirectory;

    /**
     * Path to converter settings file (which normally has a .dat extension).
     */
    //@Parameter(property = "converterSettings", required = true)
    File converterSettings;

    /**
     * If true, don't do anything.  Defaults to the skipConversion global property.  Thus "mvn -DskipConversion" will
     * skip the Java -> C# conversion.
     */
    //@Parameter(defaultValue = "${skipConversion}", property = "skipConversion", required = false)
    boolean skip;

    /**
     * If true (the default), run the converter in a minimized window via "cmd /c start /min".  That avoids the
     * converter window flashing for an instant.
     */
    //@Parameter(property = "runMinimized", defaultValue = "true", required = false)
    boolean runMinimized = true;


    @TaskAction
    public void convert() {
        if (skip)
            return;

        JavaToCSharpImpl javaToCSharpImpl = new JavaToCSharpImpl(getLogger());

        javaToCSharpImpl.convert(sourceDirectory, outputDirectory, converterDirectory, converterSettings, runMinimized);

        if (testSourceDirectory.exists()) {
            javaToCSharpImpl.convert(testSourceDirectory, testOutputDirectory, converterDirectory, converterSettings, runMinimized);
        }
    }
}
