package com.mig82.folders;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.mig82.folders.properties.FolderProperties;
import com.mig82.folders.properties.StringProperty;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;


public class FolderPropertiesTest {
	@Rule
	public JenkinsRule r = new JenkinsRule();
	private Folder f;

	@Before
	public void setUp() throws IOException {
		f = r.jenkins.createProject(Folder.class, "f");
		FolderProperties properties = new FolderProperties();
		properties.setProperties(new StringProperty[]{
				new StringProperty("key1", "value1"),
				new StringProperty("key2", "value2")
		});
		f.addProperty(properties);
	}

	@Test
	public void testPipelineInNode() throws Exception {
		WorkflowJob p = f.createProject(WorkflowJob.class, "p");
		String script =
				"node {\n" +
						"  wrap([$class: 'ParentFolderBuildWrapper']) {\n" +
						"    echo(\"key1: ${env.key1}\")\n" +
						"  }\n" +
						"}";
		p.setDefinition(new CpsFlowDefinition(script, true));
		WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
		r.assertLogContains("key1: value1", b);
	}

	@Test
	public void testPipelineInNodeNoWrap() throws Exception {
		WorkflowJob p = f.createProject(WorkflowJob.class, "p");
		String script =
				"node {\n" +
						"  withFolderProperties {\n" +
						"    echo(\"key1: ${env.key1}\")\n" +
						"  }\n" +
						"}";
		p.setDefinition(new CpsFlowDefinition(script, true));
		WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
		r.assertLogContains("key1: value1", b);
	}

	@Test
	public void testPipelineOutNode() throws Exception {
		WorkflowJob p = f.createProject(WorkflowJob.class, "p");
		String script =
				"  withFolderProperties {\n" +
						"    echo(\"key1: ${env.key1}\")\n" +
						"}";
		p.setDefinition(new CpsFlowDefinition(script, true));
		WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
		r.assertLogContains("key1: value1", b);
	}

	@Test
	public void testFreestyle() throws Exception {

		//Create a freestyle project which attempts to use props from parent folder.
		FreeStyleProject p = FreestyleTestHelper.createJob(f, "p");
		FreestyleTestHelper.addEcho(p, "key1");
		FreestyleTestHelper.addEcho(p, "key2");

		//Run the build.
		FreeStyleBuild b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));

		//Check that both properties were accessible.
		r.assertLogContains("key1: value1", b);
		r.assertLogContains("key2: value2", b);
	}

	@Test
	public void testFreestyleInSubFolder() throws Exception {

		//Create a subfolder.
		Folder sub = f.createProject(Folder.class, "sub");

		//Add a property in the subfolder that overrides another in the parent folder.
		FolderProperties properties = new FolderProperties();
		properties.setProperties(new StringProperty[]{
				new StringProperty("key1", "override")
		});
		sub.addProperty(properties);

		//Create a freestyle project in the subfolder that attempts to use props from parent and grandparent.
		FreeStyleProject p = FreestyleTestHelper.createJob(sub, "p");
		FreestyleTestHelper.addEcho(p, "key1");
		FreestyleTestHelper.addEcho(p, "key2");

		//Run the build.
		FreeStyleBuild b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));

		//Check that both properties were accessible.
		r.assertLogContains("key1: override", b);
		r.assertLogContains("key2: value2", b);
	}

	@Test
	public void testPipelineInSubFolder() throws Exception {
		Folder sub = f.createProject(Folder.class, "sub");
		FolderProperties properties = new FolderProperties();
		properties.setProperties(new StringProperty[]{new StringProperty("key1", "override")});
		sub.addProperty(properties);
		WorkflowJob p = sub.createProject(WorkflowJob.class, "p");
		String script =
				"node {\n" +
						"  wrap([$class: 'ParentFolderBuildWrapper']){\n" +
						"    echo(\"key1: ${env.key1}\")\n" +
						"    echo(\"key2: ${env.key2}\")\n" +
						"  }\n" +
						"}";
		p.setDefinition(new CpsFlowDefinition(script, true));
		WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
		r.assertLogContains("key1: override", b);
		r.assertLogContains("key2: value2", b);
	}

	@Test
	public void testPipelineOverrideEnv() throws Exception {
		WorkflowJob p = f.createProject(WorkflowJob.class, "p");
		String script =
				"withEnv(['key1=old']) {\n" +
						"  node {\n" +
						"    wrap([$class: 'ParentFolderBuildWrapper']){\n" +
						"      echo(\"key1: ${env.key1}\")\n" +
						"    }\n" +
						"  }\n" +
						"}";
		p.setDefinition(new CpsFlowDefinition(script, true));
		WorkflowRun b = r.assertBuildStatusSuccess(p.scheduleBuild2(0));
		r.assertLogContains("key1: value1", b);
	}
}