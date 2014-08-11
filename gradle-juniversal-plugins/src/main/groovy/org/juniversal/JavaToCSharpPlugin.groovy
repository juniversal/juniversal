package org.juniversal;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Bret Johnson
 * @since 7/8/2014 10:39 PM
 */
class JavaToCSharpPlugin implements Plugin<Project> {
    void apply(Project target) {
        target.task('javaToCSharp', type: JavaToCSharpTask)
    }
}
