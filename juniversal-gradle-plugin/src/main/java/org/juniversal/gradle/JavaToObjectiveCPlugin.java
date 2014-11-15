package org.juniversal.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Bret Johnson
 * @since 7/8/2014 10:39 PM
 */
class JavaToObjectiveCPlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        //target.task('javaToObjectiveC', type: JavaToObjectiveCTask)
        target.getTasks().create("javaToObjectiveC", JavaToObjectiveCTask.class);
    }
}
