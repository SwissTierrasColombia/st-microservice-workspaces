package com.ai.st.microservice.workspaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.business.MilestoneBusiness;
import com.ai.st.microservice.workspaces.entities.DepartmentEntity;
import com.ai.st.microservice.workspaces.entities.MilestoneEntity;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.services.IDepartmentService;
import com.ai.st.microservice.workspaces.services.IMilestoneService;
import com.ai.st.microservice.workspaces.services.IMunicipalityService;

@Component
public class StMicroserviceWorkspacesApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger log = LoggerFactory.getLogger(StMicroserviceWorkspacesApplicationStartup.class);

	@Autowired
	private IDepartmentService departmentService;

	@Autowired
	private IMunicipalityService municipalityService;

	@Autowired
	private IMilestoneService milestoneService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.info("ST - Loading Domains ... ");
		this.initMunicipalities();
		this.initMilestones();
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
				municipalitySincelejo.setCode("001");
				municipalitySincelejo.setDepartment(departmentSucre);
				municipalityService.createMunicipality(municipalitySincelejo);

				MunicipalityEntity municipalityBuenavista = new MunicipalityEntity();
				municipalityBuenavista.setName("BUENAVISTA");
				municipalityBuenavista.setCode("110");
				municipalityBuenavista.setDepartment(departmentSucre);
				municipalityService.createMunicipality(municipalityBuenavista);

				MunicipalityEntity municipalityCaimito = new MunicipalityEntity();
				municipalityCaimito.setName("CAIMITO");
				municipalityCaimito.setCode("124");
				municipalityCaimito.setDepartment(departmentSucre);
				municipalityService.createMunicipality(municipalityCaimito);

				MunicipalityEntity municipalityCorozal = new MunicipalityEntity();
				municipalityCorozal.setName("COROZAL");
				municipalityCorozal.setCode("215");
				municipalityCorozal.setDepartment(departmentSucre);
				municipalityService.createMunicipality(municipalityCorozal);

				MunicipalityEntity municipalityCovenas = new MunicipalityEntity();
				municipalityCovenas.setName("COVEÑAS");
				municipalityCovenas.setCode("221");
				municipalityCovenas.setDepartment(departmentSucre);
				municipalityService.createMunicipality(municipalityCovenas);

				MunicipalityEntity municipalityChalan = new MunicipalityEntity();
				municipalityChalan.setName("CHALÁN");
				municipalityChalan.setCode("230");
				municipalityChalan.setDepartment(departmentSucre);
				municipalityService.createMunicipality(municipalityChalan);

				MunicipalityEntity municipalityOvejas = new MunicipalityEntity();
				municipalityOvejas.setName("OVEJAS");
				municipalityOvejas.setCode("508");
				municipalityOvejas.setDepartment(departmentSucre);
				municipalityService.createMunicipality(municipalityOvejas);

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

}
