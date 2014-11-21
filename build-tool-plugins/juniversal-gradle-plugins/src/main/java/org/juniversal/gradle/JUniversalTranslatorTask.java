package org.juniversal.gradle;

import org.juniversal.common.JUniversalTranslator;

import java.io.File;

public class JUniversalTranslatorTask extends TranslatorTask {
    private File juniversalTranslatorJar = null;

    public JUniversalTranslatorTask(String defaultOutputDirectoryName) {
        super(defaultOutputDirectoryName);
    }

    protected void initTranslator(JUniversalTranslator translator) {
        super.initTranslator(translator);
        translator.setJuniversalTranslatorJar(juniversalTranslatorJar);
    }
}
