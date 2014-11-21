package org.juniversal.common;

import org.juniversal.common.support.*;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Bret Johnson on 11/15/2014.
 */
public class JUniversalTranslator extends Translator {
    private File juniversalTranslatorJar;
    private String targetLanguage;

    public JUniversalTranslator(CommonProject project, String targetLanguage) {
        super(project);
        this.targetLanguage = targetLanguage;
    }

    public void setJuniversalTranslatorJar(File juniversalTranslatorJar) {
        this.juniversalTranslatorJar = juniversalTranslatorJar;
    }

    public void translateSourceType(SourceType sourceType, File outputDirectory) {
        // Delete the generated source directories
        Utils.deleteChildDirectoriesExcept(outputDirectory, "nontranslated");

        ArrayList<String> args = new ArrayList<String>();

        args.add("-l");
        args.add(targetLanguage);

        FileSet classpath = getProject().getClasspath(sourceType);
        FileSet sourceDirectories = getProject().getTranslatableSourceDirectories(sourceType);

        if (! classpath.isEmpty()) {
            args.add("-classpath");
            args.add(classpath.getAsPath());
        }

        args.add("-o");
        args.add(outputDirectory.getPath());

        for (File file : sourceDirectories.getFiles()) {
            args.add(file.getAbsolutePath());
        }

        // Log the arguments
        getProject().info("Calling juniversal-translator with: " + args.toString());

        String[] argsArray = new String[args.size()];
        args.toArray(argsArray);

        try {
            org.juniversal.translator.core.Translator.main(argsArray);
        } catch (Throwable e) {
            getProject().error(e.getMessage());
        }
    }
}
