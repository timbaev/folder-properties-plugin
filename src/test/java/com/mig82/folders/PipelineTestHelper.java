package com.mig82.folders;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.Descriptor;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.io.IOException;

public class PipelineTestHelper {

	public static WorkflowJob createJob(Folder parent, String name, String script) throws IOException, Descriptor.FormException {
		WorkflowJob p = parent.createProject(WorkflowJob.class, name);
		p.setDefinition(new CpsFlowDefinition(script, true));
		return p;
	}
}
