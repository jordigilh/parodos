package com.redhat.parodos.workflow.execution.service;

import java.util.UUID;

import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;

import org.springframework.scheduling.annotation.Async;

public interface WorkFlowExecutor {

	@Async
	void executeAsync(UUID projectId, String workflowName, WorkContext workContext, UUID executionId);

	WorkReport execute(UUID projectId, String workflowName, WorkContext workContext, UUID executionId);

}
