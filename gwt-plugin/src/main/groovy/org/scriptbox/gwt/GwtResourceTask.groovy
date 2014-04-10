package org.scriptbox.gwt;

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskAction

class GwtResourcesTask extends Copy {

    def outputDir;
    def reader;
 
    public void init() {
        reader = new DefaultGwtModuleReader( project );
    }

    @TaskAction
    public void copy() {

    }
}

