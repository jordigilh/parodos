package com.redhat.parodos.examples.vmmigration.task;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.HashMap;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.redhat.parodos.examples.base.BaseInfrastructureWorkFlowTaskTest;
import com.redhat.parodos.examples.vmmigration.constants.Constants;
import com.redhat.parodos.examples.vmmigration.dto.io.konveyor.forklift.v1beta1.Migration;
import com.redhat.parodos.examples.vmmigration.dto.io.konveyor.forklift.v1beta1.Plan;
import com.redhat.parodos.examples.vmmigration.util.Kubernetes;
import com.redhat.parodos.workflow.context.WorkContextDelegate;
import com.redhat.parodos.workflow.task.infrastructure.BaseInfrastructureWorkFlowTask;
import com.redhat.parodos.workflows.work.WorkContext;
import com.redhat.parodos.workflows.work.WorkReport;
import com.redhat.parodos.workflows.work.WorkStatus;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.server.mock.OpenShiftServer;

public class CreateMigrationWorkFlowTaskTest extends BaseInfrastructureWorkFlowTaskTest {

	private static WorkContext ctx;

	public OpenShiftServer mockServer;

	private CreateMigrationWorkFlowTask createMigrationWorkFlowTask;



	private static final String[] requiredParamKeys = { Constants.KUBERNETES_API_SERVER_URL_PARAMETER_NAME,
			Constants.KUBERNETES_TOKEN_PARAMETER_NAME, Constants.STORAGE_NAME_PARAMETER_NAME,
			Constants.NETWORK_NAME_PARAMETER_NAME, Constants.NAMESPACE_NAME_PARAMETER_NAME,
			Constants.DESTINATION_PROVIDER_TYPE_PARAMETER_NAME, Constants.SOURCE_PROVIDER_TYPE_PARAMETER_NAME,
			Constants.KUBERNETES_CA_CERT_PARAMETER_NAME, Constants.VM_NAME_PARAMETER_NAME };

	@Before
	public void setUp() {
		this.createMigrationWorkFlowTask = spy((CreateMigrationWorkFlowTask) new CreateMigrationWorkFlowTask());

		createMigrationWorkFlowTask.setBeanName("createMigrationFlow");
		ctx = new WorkContext();
		HashMap<String, String> map = new HashMap<>();
		for (String paramKey : requiredParamKeys) {
			map.put(paramKey, paramKey);
			WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_TASK_EXECUTION,
					createMigrationWorkFlowTask.getName(), WorkContextDelegate.Resource.ARGUMENTS, map);
		}
		WorkContextDelegate.write(ctx, WorkContextDelegate.ProcessType.WORKFLOW_EXECUTION,
				WorkContextDelegate.Resource.ID, UUID.randomUUID());
		mockServer = new OpenShiftServer(false, true);
		mockServer.before();
		doReturn(mockServer.getKubernetesClient()).when(this.createMigrationWorkFlowTask).getKubernetesClient(
				eq(Constants.KUBERNETES_API_SERVER_URL_PARAMETER_NAME), eq(Constants.KUBERNETES_TOKEN_PARAMETER_NAME),
				eq(Constants.KUBERNETES_CA_CERT_PARAMETER_NAME));
		}

	@After
	public void tearDown() {
		mockServer.after();
	}

	@Override
	protected BaseInfrastructureWorkFlowTask getConcretePersonImplementation() {
		return new CreateMigrationWorkFlowTask();
	}

	@Test
	public void executeSuccess() {

		// given
		Plan expectedPlan = Kubernetes.createPlan(Constants.VM_NAME_PARAMETER_NAME, Constants.STORAGE_NAME_PARAMETER_NAME,
				Constants.NETWORK_NAME_PARAMETER_NAME, Constants.NAMESPACE_NAME_PARAMETER_NAME,
				Constants.DESTINATION_PROVIDER_TYPE_PARAMETER_NAME, Constants.SOURCE_PROVIDER_TYPE_PARAMETER_NAME);
		// when
		createMigrationWorkFlowTask.preExecute(ctx);
		WorkReport workReport = this.createMigrationWorkFlowTask.execute(ctx);
		// then
		assertEquals(WorkStatus.COMPLETED, workReport.getStatus());


	   OpenShiftClient client= mockServer.getOpenShiftMockServer().createOpenShiftClient();
		// validate plans
		MixedOperation<Plan, KubernetesResourceList<Plan>, Resource<Plan>> planClient = client
				.resources(Plan.class);
		KubernetesResourceList<Plan> plans= planClient.inNamespace(Constants.NAMESPACE_NAME_PARAMETER_NAME).list();
		assertEquals(plans.getItems().size(),1);
		Plan actualPlan = plans.getItems().get(0);

		then(actualPlan.getSpec()).usingRecursiveComparison().isEqualTo(expectedPlan.getSpec());
		assertTrue(actualPlan.getMetadata().getName().startsWith(Constants.VM_NAME_PARAMETER_NAME));

		// validate migration
		MixedOperation<Migration, KubernetesResourceList<Migration>, Resource<Migration>> migrationClient = client
				.resources(Migration.class);
		KubernetesResourceList<Migration> migrations = migrationClient.inNamespace(Constants.NAMESPACE_NAME_PARAMETER_NAME).list();
		assertEquals(migrations.getItems().size(),1);
		Migration actualMigration = migrations.getItems().get(0);
		Migration expectedMigration = Kubernetes.createMigration(actualPlan.getMetadata().getName(),Constants.NAMESPACE_NAME_PARAMETER_NAME);
		Assertions.assertThat(actualMigration.getSpec()).usingRecursiveComparison().isEqualTo(expectedMigration.getSpec());
		assertEquals(expectedMigration.getMetadata().getName(),actualMigration.getMetadata().getName());

	}

}
