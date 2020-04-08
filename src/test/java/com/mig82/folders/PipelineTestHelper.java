package com.mig82.folders;

import com.cloudbees.hudson.plugins.folder.Folder;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.io.IOException;

public class PipelineTestHelper {

	public static WorkflowJob createJob(Folder parent, String name, String script) throws IOException{
		WorkflowJob p = parent.createProject(WorkflowJob.class, name);
		p.setDefinition(new CpsFlowDefinition(script, true));
		return p;
	}
}
