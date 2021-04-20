package com.ai.st.microservice.workspaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.business.IntegrationStateBusiness;
import com.ai.st.microservice.workspaces.entities.DepartmentEntity;
import com.ai.st.microservice.workspaces.entities.IntegrationStateEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.services.IDepartmentService;
import com.ai.st.microservice.workspaces.services.IIntegrationStateService;
import com.ai.st.microservice.workspaces.services.IMunicipalityService;

@Component
public class StMicroserviceWorkspacesApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

	@Value("${spring.profiles.active}")
	private String activeProfile;

	private static final Logger log = LoggerFactory.getLogger(StMicroserviceWorkspacesApplicationStartup.class);

	@Autowired
	private IDepartmentService departmentService;

	@Autowired
	private IMunicipalityService municipalityService;

	@Autowired
	private IIntegrationStateService integrationStateService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.info("ST - Loading Domains ... ");

		this.initIntegrationStates();

		if (activeProfile.equalsIgnoreCase("develop")) {
			this.initMunicipalities();
		}

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

				IntegrationStateEntity stateGeneratingProduct = new IntegrationStateEntity();
				stateGeneratingProduct.setId(IntegrationStateBusiness.STATE_GENERATING_PRODUCT);
				stateGeneratingProduct.setName("GENERANDO PRODUCTO");
				stateGeneratingProduct.setDescription("Se esta generando un producto.");
				integrationStateService.createIntegrationState(stateGeneratingProduct);

				IntegrationStateEntity stateProductGenerated = new IntegrationStateEntity();
				stateProductGenerated.setId(IntegrationStateBusiness.STATE_GENERATED_PRODUCT);
				stateProductGenerated.setName("PRODUCTO GENERADO");
				stateProductGenerated.setDescription("Se ha generado ya un producto.");
				integrationStateService.createIntegrationState(stateProductGenerated);

				IntegrationStateEntity stateError = new IntegrationStateEntity();
				stateError.setId(IntegrationStateBusiness.STATE_ERROR_INTEGRATION_AUTOMATIC);
				stateError.setName("ERROR REALIZANDO INTEGRACIÓN AUTOMÁTICA");
				stateError.setDescription("Ha ocurrido un error realizando la integración.");
				integrationStateService.createIntegrationState(stateError);

				IntegrationStateEntity stateErrorProduct = new IntegrationStateEntity();
				stateErrorProduct.setId(IntegrationStateBusiness.STATE_ERROR_GENERATING_PRODUCT);
				stateErrorProduct.setName("ERROR GENERANDO PRODUCTO");
				stateErrorProduct.setDescription("Ha ocurrido un error realizando la integración.");
				integrationStateService.createIntegrationState(stateErrorProduct);

				log.info("The domains 'integration states' have been loaded!");
			} catch (Exception e) {
				log.error("Failed to load 'integration states' domains");
			}

		}

	}

}
