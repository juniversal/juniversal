package org.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test
import org.juniversal.gradle.JavaToCSharpTangibleTask

import static org.junit.Assert.assertTrue

/**
 * @author Bret Johnson
 * @since 7/7/2014 6:02 PM
 */

class JavaToCSharpTangibleTaskTest {
    @Test
    public void canAddTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        def task = project.task('javaToCSharp', type: JavaToCSharpTangibleTask)
        assertTrue(task instanceof JavaToCSharpTangibleTask)
    }
}
