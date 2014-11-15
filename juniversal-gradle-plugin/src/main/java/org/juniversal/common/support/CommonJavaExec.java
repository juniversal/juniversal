package org.juniversal.common.support;

/**
 * Created by Bret Johnson on 11/11/2014.
 */
public abstract class CommonJavaExec {
    public abstract void addArg(String... args);
    
    public abstract  String getCommandLine();

    public abstract void exec();
}
