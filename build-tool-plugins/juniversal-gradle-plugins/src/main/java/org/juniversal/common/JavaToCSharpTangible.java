package org.juniversal.common;

import org.juniversal.common.support.CommonProject;
import org.juniversal.common.support.SourceType;
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
public class JavaToCSharpTangible extends Translator {
    private File converterDirectory;
    private File converterSettings;
    private boolean runMinimized;

    public JavaToCSharpTangible(CommonProject project) {
        super(project);
    }

    public void setConverterDirectory(File converterDirectory) {
        this.converterDirectory = converterDirectory;
    }

    public void setConverterSettings(File converterSettings) {
        this.converterSettings = converterSettings;
    }

    public void setRunMinimized(boolean runMinimized) {
        this.runMinimized = runMinimized;
    }

    // TODO: Check into why converterSettings not used
    protected void translateSourceType(SourceType sourceType, File outputDirectory) {
        // Delete the generated source directories
        Utils.deleteChildDirectoriesExcept(outputDirectory, "Properties", "nontranslated", "Bin", "obj");

        ArrayList<String> args = new ArrayList<String>();

        if (runMinimized) {
            args.add("cmd.exe");
            args.add("/c");
            args.add("start");
            args.add("/min");
            args.add("/b");
            args.add("C# Converter");
        }

        args.add(new File(converterDirectory, "Java to C# Converter.exe").getPath());

        args.add(getProject().getTranslatableSourceDirectories(sourceType).getSingleElement().getAbsolutePath());
        args.add(outputDirectory.getAbsolutePath());
        args.add(converterDirectory.getAbsolutePath());

        Process process = getProject().exec(args, converterDirectory);

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String inputLine;
        try {
            while ((inputLine = reader.readLine()) != null) {
                getProject().info(inputLine);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException("IO exception when running Java to C# converter", e);
        }
    }
}

