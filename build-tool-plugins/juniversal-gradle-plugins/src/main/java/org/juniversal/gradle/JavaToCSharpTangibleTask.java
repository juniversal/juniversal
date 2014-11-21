package org.juniversal.gradle;

import org.gradle.api.tasks.TaskAction;
import org.juniversal.common.JavaToCSharpTangible;
import org.juniversal.gradle.support.GradleProject;

import java.io.File;

public class JavaToCSharpTangibleTask extends TranslatorTask {
    /**
     * Directory that contains the converter.  Java to C# Converter.exe must be in this directory.
     */
    public File converterDirectory;

    /**
     * Path to converter settings file (which normally has a .dat extension).
     */
    public File converterSettings;

    /**
     * If true (the default), run the converter in a minimized window via "cmd /c start /min".  That avoids the
     * converter window flashing for an instant.
     */
    public boolean runMinimized = true;


    public JavaToCSharpTangibleTask() {
        super("c#");
    }

    @TaskAction
    public void convert() {
        GradleProject commonProject = new GradleProject(getProject());

        JavaToCSharpTangible javaToCSharpTangible = new JavaToCSharpTangible(commonProject);
        initTranslator(javaToCSharpTangible);
        javaToCSharpTangible.setConverterDirectory(converterDirectory);
        javaToCSharpTangible.setConverterSettings(converterSettings);
        javaToCSharpTangible.setRunMinimized(runMinimized);

        javaToCSharpTangible.translate();
    }

}
