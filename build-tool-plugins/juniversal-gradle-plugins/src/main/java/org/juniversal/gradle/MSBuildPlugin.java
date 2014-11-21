package org.juniversal.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
* Created by Bret on 11/16/2014.
*/
public class MSBuildPlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        target.getTasks().create("msbuild", MSBuildTask.class);
    }
}
