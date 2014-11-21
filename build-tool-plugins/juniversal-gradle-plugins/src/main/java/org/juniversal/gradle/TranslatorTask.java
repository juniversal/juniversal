package org.juniversal.gradle;

import org.gradle.api.DefaultTask;
import org.juniversal.common.JavaToObjectiveC;
import org.juniversal.common.Translator;

import java.io.File;

public class TranslatorTask extends DefaultTask {
    /**
     * If true, don't do anything.  Defaults to the skipConversion global property.  Thus "mvn -DskipConversion" will
     * skip the Java to C# conversion.
     */
    //@Parameter(defaultValue = "${skipConversion}", property = "skipConversion", required = false)
    public boolean skip;

    /**
     * Directory for generated C# for main source.  Defaults to ${project.basedir}/c#.
     */
    //@Parameter(defaultValue = "${project.basedir}/c#", property = "outputDirectory", required = false)
    public File outputDirectory;

    /**
     * Directory for generated C# for test source.  Defaults to ${project.basedir}/c#-test.
     */
    //@Parameter(defaultValue = "${project.basedir}/c#-test", property = "testOutputDirectory", required = false)
    public File testOutputDirectory;


    public TranslatorTask(String defaultOutputDirectoryName) {
        outputDirectory = getProject().file(defaultOutputDirectoryName);
        testOutputDirectory = getProject().file(defaultOutputDirectoryName + "-test");
    }

    protected void initTranslator(Translator translator) {
        translator.setSkip(skip);
        translator.setOutputDirectory(outputDirectory);
        translator.setTestOutputDirectory(testOutputDirectory);
    }
}
