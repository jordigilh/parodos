package com.redhat.parodos.workflow.execution.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.redhat.parodos.project.dto.ProjectResponseDTO;
import com.redhat.parodos.project.service.ProjectService;
import com.redhat.parodos.workflow.WorkFlowDelegate;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.definition.dto.WorkFlowDefinitionResponseDTO;
import com.redhat.parodos.workflow.definition.entity.WorkFlowDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowTaskDefinition;
import com.redhat.parodos.workflow.definition.entity.WorkFlowWorkDefinition;
import com.redhat.parodos.workflow.definition.repository.WorkFlowDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowTaskDefinitionRepository;
import com.redhat.parodos.workflow.definition.repository.WorkFlowWorkRepository;
import com.redhat.parodos.workflow.definition.service.WorkFlowDefinitionServiceImpl;
import com.redhat.parodos.workflow.enums.WorkType;
import com.redhat.parodos.workflow.execution.dto.WorkFlowContextResponseDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowRequestDTO;
import com.redhat.parodos.workflow.execution.dto.WorkFlowStatusResponseDTO;
import com.redhat.parodos.workflow.execution.dto.WorkStatusResponseDTO;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecution;
import com.redhat.parodos.workflow.execution.entity.WorkFlowExecutionContext;
import com.redhat.parodos.workflow.execution.entity.WorkFlowTaskExecution;
import com.redhat.parodos.workflow.execution.repository.WorkFlowRepository;
import com.redhat.parodos.workflow.execution.repository.WorkFlowTaskRepository;
import com.redhat.parodos.workflow.option.WorkFlowOption;
import com.redhat.parodos.workflows.work.DefaultWorkReport;
import com.redhat.parodos.workflows.work.Work;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;
import com.redhat.parodos.workflows.workflow.SequentialFlow;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.MeterNotFoundException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class WorkFlowServiceImplTest {

	private static final String TEST_WORKFLOW_NAME = "test-workflow";

	private WorkFlowDelegate workFlowDelegate;

	private WorkFlowServiceDelegate workFlowServiceDelegate;

	private WorkFlowDefinitionRepository workFlowDefinitionRepository;

	private WorkFlowTaskDefinitionRepository workFlowTaskDefinitionRepository;

	private WorkFlowRepository workFlowRepository;

	private WorkFlowTaskRepository workFlowTaskRepository;

	private WorkFlowWorkRepository workFlowWorkRepository;

	private WorkFlowServiceImpl workFlowService;

	private WorkFlowDefinitionServiceImpl workFlowDefinitionService;

	private ProjectService projectService;

	private MeterRegistry metricRegistry;

	private ModelMapper modelMapper;

	private WorkFlowExecutor workFlowExecutor;

	@BeforeEach
	void initEach() {
		this.workFlowDelegate = mock(WorkFlowDelegate.class);
		this.workFlowServiceDelegate = mock(WorkFlowServiceDelegate.class);
		this.workFlowRepository = mock(WorkFlowRepository.class);
		this.workFlowDefinitionRepository = mock(WorkFlowDefinitionRepository.class);
		this.workFlowTaskDefinitionRepository = mock(WorkFlowTaskDefinitionRepository.class);
		this.workFlowTaskRepository = mock(WorkFlowTaskRepository.class);
		this.workFlowWorkRepository = mock(WorkFlowWorkRepository.class);
		this.workFlowDefinitionService = mock(WorkFlowDefinitionServiceImpl.class);
		this.metricRegistry = new SimpleMeterRegistry();
		this.projectService = mock(ProjectService.class);
		this.modelMapper = new ModelMapper();
		this.workFlowExecutor = new WorkFlowExecutorImpl(this.workFlowDelegate);
		this.workFlowService = new WorkFlowServiceImpl(this.workFlowDelegate, this.workFlowServiceDelegate,
				this.workFlowDefinitionRepository, this.workFlowTaskDefinitionRepository, this.workFlowRepository,
				this.workFlowTaskRepository, this.workFlowWorkRepository, this.workFlowDefinitionService,
				this.metricRegistry, this.projectService, this.modelMapper, workFlowExecutor);
	}

	@Test
	void executeTestWithValidData() {
		// given
		Work work = mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();

		when(work.execute(any())).thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, new WorkContext() {
			{
				put("foo", "bar");
			}
		}));
		when(this.workFlowDelegate.getWorkFlowExecutionByName(TEST_WORKFLOW_NAME)).thenReturn(workFlow);
		when(this.workFlowDelegate.initWorkFlowContext(any(), any())).thenReturn(new WorkContext());
		when(this.workFlowDefinitionRepository.findFirstByName(any()))
				.thenReturn(this.sampleWorkflowDefinition("test"));

		// when
		WorkReport report = this.workFlowExecutor.execute(UUID.randomUUID(), TEST_WORKFLOW_NAME, new WorkContext(),
				UUID.randomUUID());

		// then
		assertNotNull(report);
		assertEquals(report.getStatus().toString(), "COMPLETED");
		assertNull(report.getError());

		assertNotNull(report.getWorkContext());
		assertEquals(report.getWorkContext().get("foo"), "bar");

		verify(this.workFlowDelegate, times(1)).getWorkFlowExecutionByName(any());
		verify(work, times(1)).execute(any());

	}

	@Test
	void executeTestWithNoValidWorkflow() {
		// given
		when(this.workFlowDelegate.getWorkFlowExecutionByName(any())).thenReturn(null);

		// when
		WorkReport report = this.workFlowService.execute(WorkFlowRequestDTO.builder().projectId(UUID.randomUUID())
				.works(List.of()).workFlowName(TEST_WORKFLOW_NAME).build());

		// then
		assertNotNull(report);
		assertEquals(report.getStatus().toString(), "FAILED");
		assertNotNull(report.getError());

		assertNotNull(report.getWorkContext());

		verify(this.workFlowDelegate, times(1)).getWorkFlowExecutionByName(any());
		verify(this.workFlowDelegate, times(0)).initWorkFlowContext(any(), any());
		verify(this.workFlowDefinitionRepository, times(0)).findFirstByName(any());
	}

	@Test
	void executeWithDTOWithValidData() {
		// given
		Work work = mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();
		when(work.execute(any())).thenReturn(new DefaultWorkReport(WorkStatus.COMPLETED, new WorkContext() {
			{
				put("foo", "bar");
			}
		}));
		when(this.workFlowDefinitionRepository.findFirstByName(any()))
				.thenReturn(this.sampleWorkflowDefinition("test"));
		when(this.workFlowWorkRepository.findFirstByWorkDefinitionId(any())).thenReturn(null);
		when(this.workFlowDelegate.initWorkFlowContext(any(), any())).thenReturn(new WorkContext());
		when(this.workFlowDelegate.getWorkFlowExecutionByName(TEST_WORKFLOW_NAME)).thenReturn(workFlow);
		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().status(WorkStatus.IN_PROGRESS).build();
		workFlowExecution.setId(UUID.randomUUID());
		when(this.workFlowRepository.save(any())).thenReturn(workFlowExecution);
		WorkFlowDefinitionResponseDTO workFlowDefinitionResponseDTO = WorkFlowDefinitionResponseDTO.builder()
				.name(TEST_WORKFLOW_NAME).works(List.of()).build();
		when(this.workFlowDefinitionService.getWorkFlowDefinitionByName(TEST_WORKFLOW_NAME))
				.thenReturn(workFlowDefinitionResponseDTO);

		// when
		WorkReport report = this.workFlowService.execute(WorkFlowRequestDTO.builder().projectId(UUID.randomUUID())
				.works(List.of()).workFlowName(TEST_WORKFLOW_NAME).build());

		// then
		assertNotNull(report);
		assertEquals("IN_PROGRESS", report.getStatus().toString());
		assertNull(report.getError());

		assertNotNull(report.getWorkContext());

		verify(this.workFlowDelegate, times(2)).getWorkFlowExecutionByName(any());
		verify(this.workFlowDelegate, times(1)).initWorkFlowContext(any(), any());
		verify(this.workFlowDefinitionRepository, times(1)).findFirstByName(any());
	}

	@Test
	void executeWithDTOWithNoMainWorkFlow() {
		// given
		Work work = mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();
		when(this.workFlowDefinitionRepository.findFirstByName(any()))
				.thenReturn(this.sampleWorkflowDefinition("test"));
		when(this.workFlowWorkRepository.findFirstByWorkDefinitionId(any()))
				.thenReturn(WorkFlowWorkDefinition.builder().build());
		when(this.workFlowDelegate.getWorkFlowExecutionByName(TEST_WORKFLOW_NAME)).thenReturn(workFlow);

		// when
		WorkReport report = this.workFlowService.execute(WorkFlowRequestDTO.builder().projectId(UUID.randomUUID())
				.works(List.of()).workFlowName(TEST_WORKFLOW_NAME).build());

		// then
		assertNotNull(report);
		assertEquals(report.getStatus().toString(), "FAILED");
		assertNotNull(report.getError());

		assertNotNull(report.getWorkContext());

		verify(this.workFlowDelegate, times(1)).getWorkFlowExecutionByName(any());
		verify(this.workFlowDelegate, times(0)).initWorkFlowContext(any(), any());
		verify(this.workFlowDefinitionRepository, times(1)).findFirstByName(any());
	}

	@Test
	void executeWithDTOWithNoWorkFlowDefinition() {
		// given
		Work work = mock(Work.class);
		SequentialFlow workFlow = SequentialFlow.Builder.aNewSequentialFlow().named("test").execute(work).build();
		when(this.workFlowDefinitionRepository.findFirstByName(any())).thenReturn(null);
		when(this.workFlowDelegate.getWorkFlowExecutionByName(TEST_WORKFLOW_NAME)).thenReturn(workFlow);

		// when
		WorkReport report = this.workFlowService.execute(WorkFlowRequestDTO.builder().projectId(UUID.randomUUID())
				.works(List.of()).workFlowName(TEST_WORKFLOW_NAME).build());

		// then
		assertNotNull(report);
		assertEquals(report.getStatus().toString(), "FAILED");
		assertNotNull(report.getError());

		assertNotNull(report.getWorkContext());

		verify(this.workFlowDelegate, times(1)).getWorkFlowExecutionByName(any());
		verify(this.workFlowDelegate, never()).initWorkFlowContext(any(), any());
		verify(this.workFlowDefinitionRepository, times(1)).findFirstByName(any());
		verify(this.workFlowWorkRepository, never()).findFirstByWorkDefinitionId(any());
	}

	@Test
	void getWorkFlowByIDTestWithValidData() {
		// given
		UUID id = UUID.randomUUID();

		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED).build();
		workFlowExecution.setId(id);
		when(this.workFlowRepository.findById(id)).thenReturn(Optional.of(workFlowExecution));

		// when
		WorkFlowExecution res = this.workFlowService.getWorkFlowById(id);

		// then
		assertNotNull(res);
		assertEquals(res.getId().toString(), id.toString());
		assertEquals(res.getStatus().toString(), "COMPLETED");
	}

	@Test
	void getWorkFlowByIDTestWithInvalidData() {
		// given
		UUID id = UUID.randomUUID();

		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED).build();
		workFlowExecution.setId(id);
		when(this.workFlowRepository.findById(id)).thenReturn(Optional.empty());

		// when
		WorkFlowExecution res = this.workFlowService.getWorkFlowById(id);
		// then
		assertNull(res);
	}

	@Test
	void testSaveWorkflow() {
		// given
		UUID projectId = UUID.randomUUID();
		UUID workflowDefID = UUID.randomUUID();

		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED).build();
		workFlowExecution.setId(UUID.randomUUID());

		WorkFlowExecution mainWorkFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED).build();
		mainWorkFlowExecution.setId(UUID.randomUUID());

		when(this.workFlowRepository.save(any())).thenReturn(workFlowExecution);

		// when
		WorkFlowExecution res = this.workFlowService.saveWorkFlow(projectId, workflowDefID, WorkStatus.COMPLETED,
				mainWorkFlowExecution, "{}");

		// then
		assertNotNull(res);

		ArgumentCaptor<WorkFlowExecution> argument = ArgumentCaptor.forClass(WorkFlowExecution.class);
		verify(this.workFlowRepository, times(1)).save(argument.capture());
		assertEquals(argument.getValue().getStatus().toString(), "COMPLETED");
		assertEquals(argument.getValue().getProjectId().toString(), projectId.toString());
		assertEquals(argument.getValue().getWorkFlowDefinitionId().toString(), workflowDefID.toString());
	}

	@Test
	void testUpdateWorkflowWithValidData() {
		// given
		UUID projectId = UUID.randomUUID();
		UUID workflowDefID = UUID.randomUUID();

		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().status(WorkStatus.COMPLETED)
				.projectId(projectId).workFlowDefinitionId(workflowDefID).build();
		workFlowExecution.setId(UUID.randomUUID());
		when(this.workFlowRepository.save(any())).thenReturn(workFlowExecution);

		// when
		WorkFlowExecution res = this.workFlowService.updateWorkFlow(workFlowExecution);

		// then
		assertNotNull(res);

		ArgumentCaptor<WorkFlowExecution> argument = ArgumentCaptor.forClass(WorkFlowExecution.class);
		verify(this.workFlowRepository, times(1)).save(argument.capture());
		assertEquals(argument.getValue().getStatus().toString(), "COMPLETED");
		assertEquals(argument.getValue().getProjectId().toString(), projectId.toString());
		assertEquals(argument.getValue().getWorkFlowDefinitionId().toString(), workflowDefID.toString());

		assertEquals(this.metricRegistry.get("workflow.executions").tag("status", "COMPLETED").counter().count(), 1);
		// No other tags are created under workflow.executions metrics
		assertEquals(this.metricRegistry.get("workflow.executions").counter().count(), 1);
		// check that IN_PROGRESS tag was not added
		assertThrows(MeterNotFoundException.class, () -> {
			this.metricRegistry.get("workflow.executions").tag("status", "IN_PROGRESS").counter();
		});
	}

	@Test
	void getWorkFlowTaskTestWithValidData() {
		// given
		UUID wfTaskDefID = UUID.randomUUID();
		UUID wfExecutionID = UUID.randomUUID();

		WorkFlowTaskExecution workFlowTaskExecution = WorkFlowTaskExecution.builder().arguments("test").results("res")
				.status(WorkStatus.COMPLETED).workFlowTaskDefinitionId(wfTaskDefID).workFlowExecutionId(wfExecutionID)
				.build();
		workFlowTaskExecution.setId(UUID.randomUUID());

		when(this.workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(wfExecutionID,
				wfTaskDefID)).thenReturn(List.of(workFlowTaskExecution));
		// when

		WorkFlowTaskExecution res = this.workFlowService.getWorkFlowTask(wfExecutionID, wfTaskDefID);
		// then
		assertNotNull(res);
		assertEquals(res.getStatus().toString(), workFlowTaskExecution.getStatus().toString());
		assertEquals(res.getWorkFlowExecutionId().toString(), wfExecutionID.toString());
		assertEquals(res.getWorkFlowTaskDefinitionId().toString(),
				workFlowTaskExecution.getWorkFlowTaskDefinitionId().toString());
		assertEquals(res.getArguments(), workFlowTaskExecution.getArguments());
		assertEquals(res.getResults(), workFlowTaskExecution.getResults());
	}

	@Test
	void getWorkFlowTaskTestWithInvalidData() {
		// given
		UUID wfTaskDefID = UUID.randomUUID();
		UUID wfExecutionID = UUID.randomUUID();
		when(this.workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(wfExecutionID,
				wfTaskDefID)).thenReturn(null);

		// when
		WorkFlowTaskExecution res = this.workFlowService.getWorkFlowTask(wfExecutionID, wfTaskDefID);
		// then
		assertNull(res);
		verify(this.workFlowTaskRepository, times(1))
				.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(wfExecutionID, wfTaskDefID);
	}

	@Test
	void getWorkFlowTaskTestWithEmptyArray() {
		// given
		UUID wfTaskDefID = UUID.randomUUID();
		UUID wfExecutionID = UUID.randomUUID();
		when(this.workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(wfExecutionID,
				wfTaskDefID)).thenReturn(new LinkedList<>());

		// when
		WorkFlowTaskExecution res = this.workFlowService.getWorkFlowTask(wfExecutionID, wfTaskDefID);
		// then
		assertNull(res);
		verify(this.workFlowTaskRepository, times(1))
				.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(wfExecutionID, wfTaskDefID);
	}

	@Test
	void testSaveWorkFlowTaskExecution() {
		// given
		UUID wfTaskDefID = UUID.randomUUID();
		UUID wfExecutionID = UUID.randomUUID();

		WorkFlowTaskExecution workFlowTaskExecution = WorkFlowTaskExecution.builder().arguments("test").results("res")
				.status(WorkStatus.COMPLETED).workFlowTaskDefinitionId(wfTaskDefID).workFlowExecutionId(wfExecutionID)
				.build();
		workFlowTaskExecution.setId(UUID.randomUUID());

		when(this.workFlowTaskRepository.save(any())).thenReturn(workFlowTaskExecution);
		// when
		WorkFlowTaskExecution res = this.workFlowService.saveWorkFlowTask("arguments", wfTaskDefID, wfExecutionID,
				WorkStatus.COMPLETED);

		// then
		assertNotNull(res);

		ArgumentCaptor<WorkFlowTaskExecution> argument = ArgumentCaptor.forClass(WorkFlowTaskExecution.class);
		verify(this.workFlowTaskRepository, times(1)).save(argument.capture());
		assertEquals(argument.getValue().getStatus().toString(), "COMPLETED");
		assertEquals(argument.getValue().getWorkFlowExecutionId().toString(), wfExecutionID.toString());
		assertEquals(argument.getValue().getWorkFlowTaskDefinitionId().toString(), wfTaskDefID.toString());
		assertEquals(argument.getValue().getArguments(), "arguments");
		assertNull(argument.getValue().getResults());
	}

	@Test
	void testUpdateWorkFlowTask() {
		// given
		UUID wfTaskDefID = UUID.randomUUID();
		UUID wfExecutionID = UUID.randomUUID();

		WorkFlowTaskExecution workFlowTaskExecution = WorkFlowTaskExecution.builder().arguments("test").results("res")
				.status(WorkStatus.COMPLETED).workFlowTaskDefinitionId(wfTaskDefID).workFlowExecutionId(wfExecutionID)
				.build();
		workFlowTaskExecution.setId(UUID.randomUUID());

		when(this.workFlowTaskRepository.save(any())).thenReturn(workFlowTaskExecution);
		// when
		WorkFlowTaskExecution res = this.workFlowService.updateWorkFlowTask(workFlowTaskExecution);

		// then
		assertNotNull(res);

		ArgumentCaptor<WorkFlowTaskExecution> argument = ArgumentCaptor.forClass(WorkFlowTaskExecution.class);
		verify(this.workFlowTaskRepository, times(1)).save(argument.capture());
		assertEquals(argument.getValue().getStatus().toString(), "COMPLETED");
		assertEquals(argument.getValue().getWorkFlowExecutionId().toString(), wfExecutionID.toString());
		assertEquals(argument.getValue().getWorkFlowTaskDefinitionId().toString(), wfTaskDefID.toString());
		assertEquals(argument.getValue().getArguments(), "test");
		assertNotNull(argument.getValue().getResults(), "res");
	}

	@Test
	void testGetWorkStatusWithValidData() {
		String WORKFLOW_NAME = "testWorkFlow";
		UUID workFlowExecutionId = UUID.randomUUID();
		UUID workFlowDefinitionId = UUID.randomUUID();

		// given
		// workflow (main)
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name(WORKFLOW_NAME).numberOfWorks(2)
				.build();
		workFlowDefinition.setId(workFlowDefinitionId);

		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().workFlowDefinitionId(workFlowDefinitionId)
				.status(WorkStatus.IN_PROGRESS).build();
		workFlowExecution.setId(workFlowExecutionId);

		// subWorkflow1
		String SUB_WORKFLOW_1_NAME = "testSubWorkFlow1";
		UUID testSubWorkFlow1DefinitionId = UUID.randomUUID();
		UUID testSubWorkFlow1ExecutionId = UUID.randomUUID();
		// subWorkflow1Definition
		WorkFlowDefinition subWorkFlow1Definition = WorkFlowDefinition.builder().name(SUB_WORKFLOW_1_NAME)
				.numberOfWorks(1).build();
		subWorkFlow1Definition.setId(testSubWorkFlow1DefinitionId);
		// subWorkflow1Execution
		WorkFlowExecution subWorkFlow1Execution = WorkFlowExecution.builder().projectId(UUID.randomUUID())
				.status(WorkStatus.IN_PROGRESS).workFlowDefinitionId(testSubWorkFlow1DefinitionId)
				.mainWorkFlowExecution(workFlowExecution).build();
		subWorkFlow1Execution.setId(testSubWorkFlow1ExecutionId);

		// subWorkflow1Task1
		String SUB_WORKFLOW_1_TASK_1_NAME = "testSubWorkFlow1Task1";
		UUID subWorkFlow1Task1DefinitionId = UUID.randomUUID();
		UUID subWorkFlow1Task1ExecutionId = UUID.randomUUID();
		// subWorkflow1Task1Definition
		WorkFlowTaskDefinition subWorkFlow1Task1Definition = WorkFlowTaskDefinition.builder()
				.name(SUB_WORKFLOW_1_TASK_1_NAME).build();
		subWorkFlow1Task1Definition.setId(subWorkFlow1Task1DefinitionId);
		// link subWorkflow1Task1Definition to subWorkflow1Definition
		subWorkFlow1Definition.setWorkFlowTaskDefinitions(List.of(subWorkFlow1Task1Definition));
		// subWorkflow1TaskExecution1
		WorkFlowTaskExecution subWorkFlow1Task1Execution = WorkFlowTaskExecution.builder()
				.status(WorkStatus.IN_PROGRESS).workFlowExecutionId(testSubWorkFlow1ExecutionId)
				.workFlowTaskDefinitionId(subWorkFlow1Task1DefinitionId).build();
		subWorkFlow1Task1Execution.setId(subWorkFlow1Task1ExecutionId);

		// workflowTask1
		String WORKFLOW_TASK_1_NAME = "testWorkFlowTask1";
		UUID workFlowTask1DefinitionId = UUID.randomUUID();
		UUID workFlowTask1ExecutionId = UUID.randomUUID();
		// workflowTask1Definition
		WorkFlowTaskDefinition workFlowTask1Definition = WorkFlowTaskDefinition.builder().name(WORKFLOW_TASK_1_NAME)
				.build();
		workFlowTask1Definition.setId(workFlowTask1DefinitionId);
		// workflowTask1Execution
		WorkFlowTaskExecution workFlowTask1Execution = WorkFlowTaskExecution.builder().status(WorkStatus.COMPLETED)
				.workFlowExecutionId(workFlowExecutionId).workFlowTaskDefinitionId(workFlowTask1DefinitionId).build();
		workFlowTask1Execution.setId(workFlowTask1ExecutionId);

		// link workflowTask1Definition to workFlowDefinition
		workFlowDefinition.setWorkFlowTaskDefinitions(List.of(workFlowTask1Definition));

		// when
		when(this.workFlowDefinitionRepository.findById(eq(workFlowDefinitionId)))
				.thenReturn(Optional.of(workFlowDefinition));

		when(this.workFlowRepository.findById(eq(workFlowExecutionId))).thenReturn(Optional.of(workFlowExecution));

		when(this.workFlowDefinitionRepository.findById(eq(testSubWorkFlow1DefinitionId)))
				.thenReturn(Optional.of(subWorkFlow1Definition));

		when(this.workFlowRepository.findFirstByMainWorkFlowExecutionAndWorkFlowDefinitionId(eq(workFlowExecution),
				eq(testSubWorkFlow1DefinitionId))).thenReturn(subWorkFlow1Execution);

		when(this.workFlowTaskDefinitionRepository.findById(eq(subWorkFlow1Task1DefinitionId)))
				.thenReturn(Optional.of(subWorkFlow1Task1Definition));

		when(this.workFlowTaskDefinitionRepository.findById(eq(workFlowTask1DefinitionId)))
				.thenReturn(Optional.of(workFlowTask1Definition));

		when(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(
				eq(testSubWorkFlow1ExecutionId), eq(subWorkFlow1Task1DefinitionId)))
						.thenReturn(List.of(subWorkFlow1Task1Execution));

		when(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(eq(workFlowExecutionId),
				eq(workFlowTask1DefinitionId))).thenReturn(List.of(workFlowTask1Execution));

		when(this.workFlowServiceDelegate.getWorkFlowAndWorksStatus(eq(workFlowExecution), eq(workFlowDefinition)))
				.thenReturn(List.of(
						WorkStatusResponseDTO.builder().name(SUB_WORKFLOW_1_NAME).type(WorkType.WORKFLOW)
								.status(WorkStatus.PENDING)
								.works(List.of(WorkStatusResponseDTO.builder().name(SUB_WORKFLOW_1_TASK_1_NAME)
										.type(WorkType.TASK).status(WorkStatus.PENDING).build()))
								.workExecution(subWorkFlow1Execution).numberOfWorks(1).build(),
						WorkStatusResponseDTO.builder().name(WORKFLOW_TASK_1_NAME).type(WorkType.TASK)
								.status(WorkStatus.COMPLETED).build()));
		// then
		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = this.workFlowService
				.getWorkFlowStatus(workFlowExecutionId);

		// workflow (main)
		assertNotNull(workFlowStatusResponseDTO);
		assertEquals(workFlowExecution.getId(), workFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertEquals(workFlowStatusResponseDTO.getWorkFlowName(), workFlowDefinition.getName());
		assertEquals(workFlowStatusResponseDTO.getStatus(), WorkStatus.IN_PROGRESS);
		assertEquals(workFlowStatusResponseDTO.getWorks().size(), 2);

		// subWorkflow1
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getType(), WorkType.WORKFLOW);
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getName(), subWorkFlow1Definition.getName());
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getStatus(), WorkStatus.PENDING);
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getWorks().size(), 1);

		// subWorkflow1Task1
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getWorks().get(0).getType(), WorkType.TASK);
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getWorks().get(0).getName(),
				subWorkFlow1Task1Definition.getName());
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getWorks().get(0).getStatus(), WorkStatus.PENDING);
		assertNull(workFlowStatusResponseDTO.getWorks().get(0).getWorks().get(0).getWorks());

		// workflowTask1
		assertEquals(workFlowStatusResponseDTO.getWorks().get(1).getType(), WorkType.TASK);
		assertEquals(workFlowStatusResponseDTO.getWorks().get(1).getName(), workFlowTask1Definition.getName());
		assertEquals(workFlowStatusResponseDTO.getWorks().get(1).getStatus(), WorkStatus.COMPLETED);
		assertNull(workFlowStatusResponseDTO.getWorks().get(1).getWorks());
	}

	@Test
	void testGetWorkFlowStatusWithInvalidExecutionData() {
		// workflow (main)
		UUID workFlowExecutionId = UUID.randomUUID();

		// when
		when(this.workFlowRepository.findById(eq(workFlowExecutionId))).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> {
			this.workFlowService.getWorkFlowStatus(workFlowExecutionId);
		});

		verify(this.workFlowRepository, times(1)).findById(any());
		verify(this.workFlowWorkRepository, never()).findByWorkFlowDefinitionIdOrderByCreateDateAsc(any());
	}

	@Test
	void testGetWorkFlowStatusWithInvalidDefinitionData() {
		// workflow (main)
		UUID workFlowExecutionId = UUID.randomUUID();
		UUID workFlowDefinitionId = UUID.randomUUID();
		WorkFlowExecution workFlowExecution = mock(WorkFlowExecution.class);

		// when
		when(this.workFlowRepository.findById(eq(workFlowExecutionId))).thenReturn(Optional.of(workFlowExecution));

		when(this.workFlowDefinitionRepository.findById(eq(workFlowDefinitionId))).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> {
			this.workFlowService.getWorkFlowStatus(workFlowExecutionId);
		});

		verify(this.workFlowRepository, times(1)).findById(any());
		verify(this.workFlowDefinitionRepository, times(1)).findById(any());
		verify(this.workFlowServiceDelegate, never()).getWorkFlowAndWorksStatus(eq(workFlowExecution), any());
	}

	@Test
	void testGetWorkFlowStatusWithNonMainWorkFlowData() {
		// workflow
		UUID workFlowExecutionId = UUID.randomUUID();
		UUID workFlowDefinitionId = UUID.randomUUID();
		WorkFlowExecution workFlowExecution = mock(WorkFlowExecution.class);
		WorkFlowDefinition workFlowDefinition = mock(WorkFlowDefinition.class);

		// when
		when(this.workFlowRepository.findById(eq(workFlowExecutionId))).thenReturn(Optional.of(workFlowExecution));

		when(this.workFlowDefinitionRepository.findById(eq(workFlowDefinitionId)))
				.thenReturn(Optional.of(workFlowDefinition));

		when(workFlowExecution.getMainWorkFlowExecution()).thenReturn(null);

		assertThrows(ResponseStatusException.class, () -> {
			this.workFlowService.getWorkFlowStatus(workFlowExecutionId);
		});

		verify(this.workFlowRepository, times(1)).findById(any());
		verify(this.workFlowDefinitionRepository, times(1)).findById(any());
		verify(this.workFlowServiceDelegate, never()).getWorkFlowAndWorksStatus(eq(workFlowExecution),
				eq(workFlowDefinition));
	}

	@Test
	void testGetWorkFlowStatusWhenSubWorkflowNotExecutedWithValidData() {
		String workFlowName = "testWorkFlow";
		UUID workFlowExecutionId = UUID.randomUUID();
		UUID workFlowDefinitionId = UUID.randomUUID();

		// workflow (main)
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name(workFlowName).numberOfWorks(2)
				.build();
		workFlowDefinition.setId(workFlowDefinitionId);

		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().workFlowDefinitionId(workFlowDefinitionId)
				.status(WorkStatus.IN_PROGRESS).build();
		workFlowExecution.setId(workFlowExecutionId);

		// subWorkflow1
		String SUB_WORKFLOW_1_NAME = "testSubWorkFlow1";
		UUID subWorkFlow1DefinitionId = UUID.randomUUID();
		UUID subWorkFlow1ExecutionId = UUID.randomUUID();
		// subWorkflow1Definition
		WorkFlowDefinition subWorkFlow1Definition = WorkFlowDefinition.builder().name(SUB_WORKFLOW_1_NAME)
				.numberOfWorks(1).build();
		subWorkFlow1Definition.setId(subWorkFlow1DefinitionId);
		// subWorkflow1Task1
		String SUB_WORKFLOW_1_TASK_1_NAME = "testSubWorkFlow1Task1";
		UUID subWorkFlow1Task1DefinitionId = UUID.randomUUID();
		// subWorkflow1Task1Definition
		WorkFlowTaskDefinition subWorkFlow1Task1Definition = WorkFlowTaskDefinition.builder()
				.name(SUB_WORKFLOW_1_TASK_1_NAME).build();
		subWorkFlow1Task1Definition.setId(subWorkFlow1Task1DefinitionId);
		// link subWorkflow1Task1 to subWorkflow1
		subWorkFlow1Definition.setWorkFlowTaskDefinitions(List.of(subWorkFlow1Task1Definition));

		// workflowTask1
		String WORKFLOW_TASK_1_NAME = "testWorkFlowTask1";
		UUID workFlowTask1DefinitionId = UUID.randomUUID();
		UUID workFlowTask1ExecutionId = UUID.randomUUID();
		// workflowTask1Definition
		WorkFlowTaskDefinition workFlowTask1Definition = WorkFlowTaskDefinition.builder().name(WORKFLOW_TASK_1_NAME)
				.build();
		workFlowTask1Definition.setId(workFlowTask1DefinitionId);
		// workflowTask1Execution
		WorkFlowTaskExecution workFlowTask1Execution = WorkFlowTaskExecution.builder().status(WorkStatus.COMPLETED)
				.workFlowExecutionId(workFlowExecutionId).workFlowTaskDefinitionId(workFlowTask1DefinitionId).build();
		workFlowTask1Execution.setId(workFlowTask1ExecutionId);
		// link workflow task definition 2 to main workFlow
		workFlowDefinition.setWorkFlowTaskDefinitions(List.of(workFlowTask1Definition));

		// when
		when(this.workFlowDefinitionRepository.findById(eq(workFlowDefinitionId)))
				.thenReturn(Optional.of(workFlowDefinition));

		when(this.workFlowRepository.findById(eq(workFlowExecutionId))).thenReturn(Optional.of(workFlowExecution));

		when(this.workFlowDefinitionRepository.findById(eq(subWorkFlow1DefinitionId)))
				.thenReturn(Optional.of(subWorkFlow1Definition));

		when(this.workFlowRepository.findFirstByMainWorkFlowExecutionAndWorkFlowDefinitionId(eq(workFlowExecution),
				eq(subWorkFlow1DefinitionId))).thenReturn(null);

		when(this.workFlowTaskDefinitionRepository.findById(eq(subWorkFlow1Task1DefinitionId)))
				.thenReturn(Optional.of(subWorkFlow1Task1Definition));

		when(this.workFlowTaskDefinitionRepository.findById(eq(workFlowTask1DefinitionId)))
				.thenReturn(Optional.of(workFlowTask1Definition));

		when(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(eq(subWorkFlow1ExecutionId),
				eq(subWorkFlow1Task1DefinitionId))).thenReturn(List.of());

		when(workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(eq(workFlowExecutionId),
				eq(workFlowTask1DefinitionId))).thenReturn(List.of());

		when(this.workFlowServiceDelegate.getWorkFlowAndWorksStatus(eq(workFlowExecution), eq(workFlowDefinition)))
				.thenReturn(List.of(
						WorkStatusResponseDTO.builder().name(SUB_WORKFLOW_1_NAME).type(WorkType.WORKFLOW)
								.status(WorkStatus.PENDING).works(Collections.emptyList()).numberOfWorks(1).build(),
						WorkStatusResponseDTO.builder().name(WORKFLOW_TASK_1_NAME).type(WorkType.TASK)
								.status(WorkStatus.COMPLETED).build()));

		// then
		WorkFlowStatusResponseDTO workFlowStatusResponseDTO = this.workFlowService
				.getWorkFlowStatus(workFlowExecutionId);

		// main workflow
		assertNotNull(workFlowStatusResponseDTO);
		assertEquals(workFlowExecution.getId(), workFlowStatusResponseDTO.getWorkFlowExecutionId());
		assertEquals(workFlowStatusResponseDTO.getWorkFlowName(), workFlowDefinition.getName());
		assertEquals(workFlowStatusResponseDTO.getStatus(), WorkStatus.IN_PROGRESS);
		assertEquals(workFlowStatusResponseDTO.getWorks().size(), 2);

		// sub workflow 1
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getType(), WorkType.WORKFLOW);
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getName(), subWorkFlow1Definition.getName());
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getStatus(), WorkStatus.PENDING);
		assertEquals(workFlowStatusResponseDTO.getWorks().get(0).getWorks().size(), 0);

		// workflow task 1
		assertEquals(workFlowStatusResponseDTO.getWorks().get(1).getType(), WorkType.TASK);
		assertEquals(workFlowStatusResponseDTO.getWorks().get(1).getName(), workFlowTask1Definition.getName());
		assertEquals(workFlowStatusResponseDTO.getWorks().get(1).getStatus(), WorkStatus.COMPLETED);
		assertNull(workFlowStatusResponseDTO.getWorks().get(1).getWorks());
	}

	@Test
	void testUpdateWorkFlowCheckerTaskStatusWithValidData() {
		// given
		// main workflow execution
		UUID workFlowExecutionId = UUID.randomUUID();
		// workflow checker task
		String workFlowCheckerTaskName = "testWorkFlowTask";
		String workFlowCheckerName = "testWorkFlowCheckerName";
		UUID workFlowCheckerDefinitionId = UUID.randomUUID();
		UUID projectId = UUID.randomUUID();

		// when
		// main workflow execution
		WorkFlowExecution mainWorkFlowExecution = WorkFlowExecution.builder().status(WorkStatus.FAILED)
				.projectId(projectId).workFlowDefinitionId(UUID.randomUUID()).build();
		mainWorkFlowExecution.setId(workFlowExecutionId);
		when(this.workFlowRepository.findById(eq(workFlowExecutionId))).thenReturn(Optional.of(mainWorkFlowExecution));

		// workflow checker definition
		WorkFlowDefinition workFlowDefinition = WorkFlowDefinition.builder().name(workFlowCheckerName).build();
		workFlowDefinition.setId(workFlowCheckerDefinitionId);
		// workflow checker task definition
		WorkFlowTaskDefinition workFlowCheckerTaskDefinition = WorkFlowTaskDefinition.builder()
				.workFlowDefinition(workFlowDefinition).name(workFlowCheckerTaskName).build();
		workFlowCheckerTaskDefinition.setId(UUID.randomUUID());
		when(this.workFlowTaskDefinitionRepository.findFirstByNameAndWorkFlowDefinitionType(any(), any()))
				.thenReturn(workFlowCheckerTaskDefinition);

		// workflow checker task execution
		WorkFlowExecution workFlowCheckerExecution = WorkFlowExecution.builder().status(WorkStatus.IN_PROGRESS)
				.projectId(projectId).workFlowDefinitionId(workFlowCheckerDefinitionId).build();
		workFlowCheckerExecution.setId(UUID.randomUUID());
		when(this.workFlowRepository.findByMainWorkFlowExecution(any())).thenReturn(List.of(workFlowCheckerExecution));

		WorkFlowTaskExecution workFlowTaskExecution = WorkFlowTaskExecution.builder().arguments("test").results("res")
				.status(WorkStatus.FAILED).workFlowTaskDefinitionId(workFlowCheckerTaskDefinition.getId())
				.workFlowExecutionId(workFlowCheckerExecution.getId()).build();
		workFlowTaskExecution.setId(UUID.randomUUID());
		when(this.workFlowTaskRepository.findByWorkFlowExecutionIdAndWorkFlowTaskDefinitionId(any(), any()))
				.thenReturn(List.of(workFlowTaskExecution));

		this.workFlowService.updateWorkFlowCheckerTaskStatus(workFlowExecutionId, workFlowCheckerTaskName,
				WorkStatus.COMPLETED);

		// then
		ArgumentCaptor<WorkFlowTaskExecution> argument = ArgumentCaptor.forClass(WorkFlowTaskExecution.class);
		verify(this.workFlowTaskRepository, times(1)).save(argument.capture());
		assertEquals(argument.getValue().getWorkFlowTaskDefinitionId(), workFlowCheckerTaskDefinition.getId());
		assertEquals(argument.getValue().getWorkFlowExecutionId(), workFlowCheckerExecution.getId());
		assertEquals(argument.getValue().getStatus(), WorkStatus.COMPLETED);
	}

	@Test
	void testUpdateWorkFlowCheckerTaskStatusWithInvalidExecutionData() {
		// given
		// main workflow execution
		UUID workFlowExecutionId = UUID.randomUUID();
		// workflow checker task
		String workFlowCheckerTaskName = "testWorkFlowTask";

		// when
		when(this.workFlowRepository.findById(eq(workFlowExecutionId))).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> {
			this.workFlowService.updateWorkFlowCheckerTaskStatus(workFlowExecutionId, workFlowCheckerTaskName,
					WorkStatus.COMPLETED);
		});

		verify(this.workFlowTaskRepository, times(0)).save(any());
	}

	@Test
	void testUpdateWorkFlowCheckerTaskStatusWithInvalidTaskData() {
		// given
		// main workflow execution
		UUID workFlowExecutionId = UUID.randomUUID();
		// workflow checker task
		String workFlowCheckerTaskName = "testWorkFlowTask";

		// when
		when(this.workFlowRepository.findById(eq(workFlowExecutionId)))
				.thenReturn(Optional.of(WorkFlowExecution.builder().status(WorkStatus.FAILED)
						.projectId(UUID.randomUUID()).workFlowDefinitionId(UUID.randomUUID()).build()));

		when(this.workFlowTaskDefinitionRepository.findFirstByNameAndWorkFlowDefinitionType(any(), any()))
				.thenReturn(null);

		// then
		assertThrows(ResponseStatusException.class, () -> {
			this.workFlowService.updateWorkFlowCheckerTaskStatus(workFlowExecutionId, workFlowCheckerTaskName,
					WorkStatus.COMPLETED);
		});

		verify(this.workFlowTaskRepository, never()).save(any());

	}

	@Test
	public void testGetWorkflowParametersWithWorkflowOptions() {
		// given
		UUID workFlowExecutionId = UUID.randomUUID();
		WorkFlowExecution workFlowExecution = mock(WorkFlowExecution.class);
		when(workFlowExecution.getId()).thenReturn(workFlowExecutionId);
		WorkFlowExecutionContext executionContext = mock(WorkFlowExecutionContext.class);
		when(workFlowExecution.getWorkFlowExecutionContext()).thenReturn(executionContext);
		WorkContext workContext = new WorkContext();
		WorkContextDelegate.write(workContext, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.WORKFLOW_OPTIONS,
				Map.of("newOptions", List.of(new WorkFlowOption.Builder("test-id", "test-workflow").build())));
		when(executionContext.getWorkContext()).thenReturn(workContext);

		when(this.workFlowRepository.findById(eq(workFlowExecutionId))).thenReturn(Optional.of(workFlowExecution));

		// when
		WorkFlowContextResponseDTO workflowParameters = this.workFlowService.getWorkflowParameters(workFlowExecutionId,
				List.of(WorkContextDelegate.Resource.WORKFLOW_OPTIONS));

		// then
		verify(this.workFlowRepository, times(1)).findById(any());
		assertNotNull(workflowParameters);
		assertNotNull(workflowParameters.getWorkFlowOptions());
		assertEquals(workFlowExecutionId, workflowParameters.getWorkFlowExecutionId());
		List<WorkFlowOption> newOptions = workflowParameters.getWorkFlowOptions().getNewOptions();
		assertNotNull(newOptions);
		assertNull(workflowParameters.getWorkFlowOptions().getUpgradeOptions());
		assertNull(workflowParameters.getWorkFlowOptions().getCurrentVersion());
		assertEquals(1, newOptions.size());
		assertEquals("test-workflow", newOptions.get(0).getWorkFlowName());
	}

	@Test
	public void testGetWorkflowParametersWithoutWorkflowOptions() {
		// given
		UUID workFlowExecutionId = UUID.randomUUID();
		WorkFlowExecution workFlowExecution = mock(WorkFlowExecution.class);
		when(workFlowExecution.getId()).thenReturn(workFlowExecutionId);
		WorkFlowExecutionContext executionContext = mock(WorkFlowExecutionContext.class);
		when(workFlowExecution.getWorkFlowExecutionContext()).thenReturn(executionContext);
		when(executionContext.getWorkContext()).thenReturn(new WorkContext());

		when(this.workFlowRepository.findById(eq(workFlowExecutionId))).thenReturn(Optional.of(workFlowExecution));

		// when
		WorkFlowContextResponseDTO workflowParameters = this.workFlowService.getWorkflowParameters(workFlowExecutionId,
				List.of(WorkContextDelegate.Resource.WORKFLOW_OPTIONS));

		// then
		verify(this.workFlowRepository, times(1)).findById(any());
		assertNotNull(workflowParameters);
		assertNotNull(workflowParameters.getWorkFlowOptions());
		assertEquals(workFlowExecutionId, workflowParameters.getWorkFlowExecutionId());
		assertNull(workflowParameters.getWorkFlowOptions().getNewOptions());
	}

	@Test
	@WithMockUser(username = "test-user")
	void getWorkFlowsByProjectId_when_projectIsFound_then_returnWorkFlowStatus() {
		String workName = "test-workflow";
		UUID projectId = UUID.randomUUID();
		UUID workflowExecutionId = UUID.randomUUID();
		WorkFlowDefinition workFlowDefinition = sampleWorkflowDefinition(workName);
		WorkFlowExecution workFlowExecution = WorkFlowExecution.builder().projectId(projectId)
				.status(WorkStatus.COMPLETED).workFlowDefinitionId(workFlowDefinition.getId()).build();
		workFlowExecution.setId(workflowExecutionId);
		List<WorkStatusResponseDTO> workStatusResponseDTOList = List
				.of(WorkStatusResponseDTO.builder().name(workName).status(WorkStatus.COMPLETED).build());
		when(workFlowRepository.findAllByProjectId(projectId)).thenReturn(List.of(workFlowExecution));
		when(projectService.getProjectByIdAndUsername(eq(projectId), nullable(String.class)))
				.thenReturn(ProjectResponseDTO.builder().id(projectId).name("test-project").build());
		when(workFlowDefinitionService.getWorkFlowDefinitionById(any()))
				.thenReturn(WorkFlowDefinitionResponseDTO.builder().name("test").build());

		assertThat(workFlowService.getWorkFlowsByProjectId(projectId)).hasSize(1).extracting("workStatus")
				.contains(WorkStatus.COMPLETED);
	}

	@Test
	@WithMockUser(username = "test-user")
	void getWorkFlows_when_projectIsFound_then_returnWorkFlowStatus() {
		String workName = "test-workflow";
		UUID project1Id = UUID.randomUUID();
		UUID project2Id = UUID.randomUUID();
		UUID workflowExecution1Id = UUID.randomUUID();
		UUID workflowExecution2Id = UUID.randomUUID();
		WorkFlowExecution workFlowExecution1 = WorkFlowExecution.builder().projectId(project1Id)
				.status(WorkStatus.COMPLETED).build();
		workFlowExecution1.setId(workflowExecution1Id);
		WorkFlowExecution workFlowExecution2 = WorkFlowExecution.builder().projectId(project2Id)
				.status(WorkStatus.FAILED).build();
		workFlowExecution2.setId(workflowExecution2Id);

		when(workFlowRepository.findAllByProjectId(project1Id)).thenReturn(List.of(workFlowExecution1));
		when(workFlowRepository.findAllByProjectId(project2Id)).thenReturn(List.of(workFlowExecution2));
		when(projectService.findProjectsByUserName(nullable(String.class)))
				.thenReturn(List.of(ProjectResponseDTO.builder().id(project1Id).name("test-project1").build(),
						ProjectResponseDTO.builder().id(project2Id).name("test-project2").build()));
		when(workFlowDefinitionService.getWorkFlowDefinitionById(any()))
				.thenReturn(WorkFlowDefinitionResponseDTO.builder().name("test").build());

		assertThat(workFlowService.getWorkFlows()).hasSize(2).extracting("workStatus").contains(WorkStatus.COMPLETED,
				WorkStatus.FAILED);
	}

	@Test
	void getWorkFlowsByProjectId_when_projectIsNotFound_then_returnException() {
		UUID projectId = UUID.randomUUID();

		when(projectService.getProjectById(projectId)).thenReturn(null);

		assertThrows(RuntimeException.class, () -> workFlowService.getWorkFlowsByProjectId(projectId));
	}

	private WorkFlowDefinition sampleWorkflowDefinition(String name) {
		WorkFlowDefinition wf = WorkFlowDefinition.builder().name(name).build();
		wf.setId(UUID.randomUUID());
		return wf;
	}

}
