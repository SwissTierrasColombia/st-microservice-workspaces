package com.ai.st.microservice.workspaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.business.IntegrationStateBusiness;
import com.ai.st.microservice.workspaces.business.MilestoneBusiness;
import com.ai.st.microservice.workspaces.business.StateBusiness;
import com.ai.st.microservice.workspaces.entities.DepartmentEntity;
import com.ai.st.microservice.workspaces.entities.IntegrationStateEntity;
import com.ai.st.microservice.workspaces.entities.MilestoneEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.entities.StateEntity;
import com.ai.st.microservice.workspaces.services.IDepartmentService;
import com.ai.st.microservice.workspaces.services.IIntegrationStateService;
import com.ai.st.microservice.workspaces.services.IMilestoneService;
import com.ai.st.microservice.workspaces.services.IMunicipalityService;
import com.ai.st.microservice.workspaces.services.IStateService;

@Component
public class StMicroserviceWorkspacesApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger log = LoggerFactory.getLogger(StMicroserviceWorkspacesApplicationStartup.class);

	@Autowired
	private IDepartmentService departmentService;

	@Autowired
	private IMunicipalityService municipalityService;

	@Autowired
	private IMilestoneService milestoneService;

	@Autowired
	private IStateService stateService;

	@Autowired
	private IIntegrationStateService integrationStateService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.info("ST - Loading Domains ... ");
		this.initMunicipalities();
		this.initMilestones();
		this.initStates();
		this.initIntegrationStates();
	}

	public void initMunicipalities() {

		Long countMunicipalities = municipalityService.getCount();
		if (countMunicipalities == 0) {

			try {

				DepartmentEntity departmentSucre = new DepartmentEntity();
				departmentSucre.setName("SUCRE");
				departmentSucre.setCode("70");
				departmentSucre = departmentService.createDepartment(departmentSucre);

				MunicipalityEntity municipalitySincelejo = new MunicipalityEntity();
				municipalitySincelejo.setName("SINCELEJO");
				municipalitySincelejo.setCode("70001");
				municipalitySincelejo.setDepartment(departmentSucre);
				municipalityService.createMunicipality(municipalitySincelejo);

				MunicipalityEntity municipalityBuenavista = new MunicipalityEntity();
				municipalityBuenavista.setName("BUENAVISTA");
				municipalityBuenavista.setCode("70110");
				municipalityBuenavista.setDepartment(departmentSucre);
				municipalityService.createMunicipality(municipalityBuenavista);

				MunicipalityEntity municipalityCaimito = new MunicipalityEntity();
				municipalityCaimito.setName("CAIMITO");
				municipalityCaimito.setCode("70124");
				municipalityCaimito.setDepartment(departmentSucre);
				municipalityService.createMunicipality(municipalityCaimito);

				MunicipalityEntity municipalityCorozal = new MunicipalityEntity();
				municipalityCorozal.setName("COROZAL");
				municipalityCorozal.setCode("70215");
				municipalityCorozal.setDepartment(departmentSucre);
				municipalityService.createMunicipality(municipalityCorozal);

				MunicipalityEntity municipalityCovenas = new MunicipalityEntity();
				municipalityCovenas.setName("COVEÑAS");
				municipalityCovenas.setCode("70221");
				municipalityCovenas.setDepartment(departmentSucre);
				municipalityService.createMunicipality(municipalityCovenas);

				MunicipalityEntity municipalityChalan = new MunicipalityEntity();
				municipalityChalan.setName("CHALÁN");
				municipalityChalan.setCode("70230");
				municipalityChalan.setDepartment(departmentSucre);
				municipalityService.createMunicipality(municipalityChalan);

				MunicipalityEntity municipalityOvejas = new MunicipalityEntity();
				municipalityOvejas.setName("OVEJAS");
				municipalityOvejas.setCode("70508");
				municipalityOvejas.setDepartment(departmentSucre);
				municipalityService.createMunicipality(municipalityOvejas);

				DepartmentEntity departmentCundinamarca = new DepartmentEntity();
				departmentCundinamarca.setName("CUNDINAMARCA");
				departmentCundinamarca.setCode("25");
				departmentCundinamarca = departmentService.createDepartment(departmentCundinamarca);

				MunicipalityEntity municipalityFacatativa = new MunicipalityEntity();
				municipalityFacatativa.setName("FACATATIVA");
				municipalityFacatativa.setCode("25269");
				municipalityFacatativa.setDepartment(departmentCundinamarca);
				municipalityService.createMunicipality(municipalityFacatativa);

				MunicipalityEntity municipalityGuaduas = new MunicipalityEntity();
				municipalityGuaduas.setName("GUADUAS");
				municipalityGuaduas.setCode("25320");
				municipalityGuaduas.setDepartment(departmentCundinamarca);
				municipalityService.createMunicipality(municipalityGuaduas);

				MunicipalityEntity municipalityLaPalma = new MunicipalityEntity();
				municipalityLaPalma.setName("LA PALMA");
				municipalityLaPalma.setCode("25394");
				municipalityLaPalma.setDepartment(departmentCundinamarca);
				municipalityService.createMunicipality(municipalityLaPalma);

				log.info("The domains 'municipalities' have been loaded!");
			} catch (Exception e) {
				log.error("Failed to load 'municipalities' domains");
			}

		}

	}

	public void initMilestones() {
		Long countMilestones = milestoneService.getCount();
		if (countMilestones == 0) {

			try {

				MilestoneEntity milestone1 = new MilestoneEntity();
				milestone1.setId(MilestoneBusiness.MILESTONE_NEW_WORKSPACE);
				milestone1.setName("NUEVO ESPACIO TRABAJO PARA UN MUNICIPIO");
				milestoneService.createMilestone(milestone1);

				MilestoneEntity milestone2 = new MilestoneEntity();
				milestone2.setId(MilestoneBusiness.MILESTONE_UPDATE_WORKSPACE);
				milestone2.setName("EDITAR ESPACIO TRABAJO PARA UN MUNICIPIO ");
				milestoneService.createMilestone(milestone2);

				MilestoneEntity milestone3 = new MilestoneEntity();
				milestone3.setId(MilestoneBusiness.MILESTONE_OPERATOR_ASSIGNMENT);
				milestone3.setName("ASIGNACIÓN DE OPERADOR A UN MUNICIPIO");
				milestoneService.createMilestone(milestone3);

				log.info("The domains 'milestones' have been loaded!");
			} catch (Exception e) {
				log.error("Failed to load 'milestones' domains");
			}

		}

	}

	public void initStates() {
		Long countStates = stateService.getCount();
		if (countStates == 0) {

			try {

				StateEntity stateStart = new StateEntity();
				stateStart.setId(StateBusiness.STATE_START);
				stateStart.setName("INICIO");
				stateStart.setDescription("Cuando se crea un espacio de trabajo a un municipio.");
				stateService.createState(stateStart);

				StateEntity stateRequested = new StateEntity();
				stateRequested.setId(StateBusiness.STATE_SUPPLIES_REQUESTED);
				stateRequested.setName("INSUMOS SOLICITADOS");
				stateRequested.setDescription("Cuando se solicitan insumos por parte del gestor.");
				stateService.createState(stateRequested);

				StateEntity stateReceived = new StateEntity();
				stateReceived.setId(StateBusiness.STATE_SUPPLIES_RECEIVED);
				stateReceived.setName("INSUMOS RECIBIDOS");
				stateReceived.setDescription("Cuando se reciben los insumos solicitados.");
				stateService.createState(stateReceived);

				StateEntity stateIntegrated = new StateEntity();
				stateIntegrated.setId(StateBusiness.STATE_SUPPLIES_INTEGRATED);
				stateIntegrated.setName("INSUMOS INTEGRADOS");
				stateIntegrated.setDescription("Cuando se integran los insumos por parte del gestor.");
				stateService.createState(stateIntegrated);

				StateEntity stateDelivered = new StateEntity();
				stateDelivered.setId(StateBusiness.STATE_SUPPLIES_DELIVERED);
				stateDelivered.setName("INSUMOS ENTREGADOS");
				stateDelivered.setDescription("Cuando se entregan los insumos generados al operador.");
				stateService.createState(stateDelivered);

				StateEntity stateDownloaded = new StateEntity();
				stateDownloaded.setId(StateBusiness.STATE_SUPPLIES_DOWNLOADED);
				stateDownloaded.setName("INSUMOS DESCARGADOS");
				stateDownloaded.setDescription("Cuando se descargan los insumos por parte del operador");
				stateService.createState(stateDownloaded);

				log.info("The domains 'states' have been loaded!");
			} catch (Exception e) {
				log.error("Failed to load 'states' domains");
			}

		}
	}

	public void initIntegrationStates() {

		Long countStates = integrationStateService.getCount();
		if (countStates == 0) {

			try {

				IntegrationStateEntity stateStartedAutomatic = new IntegrationStateEntity();
				stateStartedAutomatic.setId(IntegrationStateBusiness.STATE_STARTED_AUTOMATIC);
				stateStartedAutomatic.setName("INICIADA AUTOMÁTICA");
				stateStartedAutomatic.setDescription("La integración ha iniciado de forma automática.");
				integrationStateService.createIntegrationState(stateStartedAutomatic);

				IntegrationStateEntity stateFinishedAutomatic = new IntegrationStateEntity();
				stateFinishedAutomatic.setId(IntegrationStateBusiness.STATE_FINISHED_AUTOMATIC);
				stateFinishedAutomatic.setName("FINALIZADA AUTOMÁTICA");
				stateFinishedAutomatic.setDescription("La integración ha finalizado de forma automática.");
				integrationStateService.createIntegrationState(stateFinishedAutomatic);

				IntegrationStateEntity stateStartedAssisted = new IntegrationStateEntity();
				stateStartedAssisted.setId(IntegrationStateBusiness.STATE_STARTED_ASSISTED);
				stateStartedAssisted.setName("INICIADA ASISTIDA");
				stateStartedAssisted.setDescription("La integración ha iniciado de forma asistida.");
				integrationStateService.createIntegrationState(stateStartedAssisted);

				IntegrationStateEntity stateFinishedAssisted = new IntegrationStateEntity();
				stateFinishedAssisted.setId(IntegrationStateBusiness.STATE_FINISHED_ASSISTED);
				stateFinishedAssisted.setName("FINALIZADA ASISTIDA");
				stateFinishedAssisted.setDescription("La integración ha finalizado de forma asistida.");
				integrationStateService.createIntegrationState(stateFinishedAssisted);

				IntegrationStateEntity stateProductGenerated = new IntegrationStateEntity();
				stateProductGenerated.setId(IntegrationStateBusiness.STATE_PRODUCT_GENERATED);
				stateProductGenerated.setName("PRODUCTO GENERADO");
				stateProductGenerated.setDescription("Se ha generado ya un producto.");
				integrationStateService.createIntegrationState(stateProductGenerated);

				IntegrationStateEntity stateError = new IntegrationStateEntity();
				stateError.setId(IntegrationStateBusiness.STATE_ERROR);
				stateError.setName("ERROR");
				stateError.setDescription("Ha ocurrido un error realizando la integración.");
				integrationStateService.createIntegrationState(stateError);

				log.info("The domains 'integration states' have been loaded!");
			} catch (Exception e) {
				log.error("Failed to load 'integration states' domains");
			}

		}

	}

}
