package com.ai.st.microservice.workspaces.business;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.st.microservice.workspaces.clients.ManagerFeignClient;
import com.ai.st.microservice.workspaces.clients.OperatorFeignClient;
import com.ai.st.microservice.workspaces.dto.MunicipalityDto;
import com.ai.st.microservice.workspaces.dto.managers.MicroserviceManagerDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceAddUserToOperatorDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceCreateDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceCreateDeliverySupplyDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceCreateOperatorDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceOperatorDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceOperatorUserDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceSupplyDeliveryDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceUpdateDeliveredSupplyDto;
import com.ai.st.microservice.workspaces.dto.operators.MicroserviceUpdateOperatorDto;
import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;
import com.ai.st.microservice.workspaces.entities.MunicipalityEntity;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;
import com.ai.st.microservice.workspaces.exceptions.DisconnectedMicroserviceException;
import com.ai.st.microservice.workspaces.services.IMunicipalityService;

@Component
public class OperatorBusiness {

	private final Logger log = LoggerFactory.getLogger(OperatorBusiness.class);

	@Autowired
	private OperatorFeignClient operatorClient;

	@Autowired
	private ManagerFeignClient managerClient;

	@Autowired
	private SupplyBusiness supplyBusiness;

	@Autowired
	private IMunicipalityService municipalityService;

	public MicroserviceDeliveryDto createDelivery(Long operatorId, Long managerCode, String municipalityCode,
			String observations, List<MicroserviceCreateDeliverySupplyDto> supplies)
			throws DisconnectedMicroserviceException {

		MicroserviceDeliveryDto deliveryDto = null;

		try {

			MicroserviceCreateDeliveryDto createDeliveryDto = new MicroserviceCreateDeliveryDto();
			createDeliveryDto.setManagerCode(managerCode);
			createDeliveryDto.setMunicipalityCode(municipalityCode);
			createDeliveryDto.setObservations(observations);
			createDeliveryDto.setSupplies(supplies);

			deliveryDto = operatorClient.createDelivery(operatorId, createDeliveryDto);

		} catch (Exception e) {
			log.error("Error creando la entrega: " + e.getMessage());
			throw new DisconnectedMicroserviceException("No se ha podido crear la entrega.");
		}

		return deliveryDto;
	}

	public List<MicroserviceDeliveryDto> getDeliveriesByOperator(Long operatorId, String municipalityCode)
			throws BusinessException {

		List<MicroserviceDeliveryDto> deliveries = new ArrayList<>();

		try {
			deliveries = operatorClient.findDeliveriesByOperator(operatorId, municipalityCode);
		} catch (Exception e) {
			log.error("Error consultando las entregas: " + e.getMessage());
		}

		return deliveries;
	}

	public List<MicroserviceDeliveryDto> getDeliveriesActivesByOperator(Long operatorId) throws BusinessException {

		List<MicroserviceDeliveryDto> deliveries = new ArrayList<>();

		try {
			deliveries = operatorClient.findDeliveriesActivesByOperator(operatorId, true);

			for (MicroserviceDeliveryDto deliveryDto : deliveries) {

				try {
					MicroserviceManagerDto managerDto = managerClient.findById(deliveryDto.getManagerCode());
					deliveryDto.setManager(managerDto);
				} catch (Exception e) {
					log.error("Error consultando gestor: " + e.getMessage());
				}

				try {
					MunicipalityEntity municipalityEntity = municipalityService
							.getMunicipalityByCode(deliveryDto.getMunicipalityCode());

					MunicipalityDto municipalityDto = new MunicipalityDto();
					municipalityDto.setCode(municipalityEntity.getCode());
					municipalityDto.setId(municipalityEntity.getId());
					municipalityDto.setName(municipalityEntity.getName());
					deliveryDto.setMunicipality(municipalityDto);
				} catch (Exception e) {

				}

				List<MicroserviceSupplyDeliveryDto> supplyDeliveriesDto = deliveryDto.getSupplies();

				for (MicroserviceSupplyDeliveryDto supplyDeliveryDto : supplyDeliveriesDto) {

					try {

						MicroserviceSupplyDto supplyDto = supplyBusiness
								.getSupplyById(supplyDeliveryDto.getSupplyCode());
						supplyDeliveryDto.setSupply(supplyDto);

					} catch (Exception e) {
						log.error("Error consultando insumo: " + e.getMessage());
					}

				}

			}

		} catch (Exception e) {
			log.error("Error consultando las entregas activas: " + e.getMessage());
		}

		return deliveries;
	}

	public MicroserviceDeliveryDto updateSupplyDeliveredDownloaded(Long deliveryId, Long supplyId)
			throws DisconnectedMicroserviceException {

		MicroserviceDeliveryDto deliveryDto = null;

		try {

			MicroserviceUpdateDeliveredSupplyDto supplyDelivered = new MicroserviceUpdateDeliveredSupplyDto();
			supplyDelivered.setDownloaded(true);

			deliveryDto = operatorClient.updateSupplyDelivered(deliveryId, supplyId, supplyDelivered);

		} catch (Exception e) {
			log.error("Error actualizando la fecha de descarga del insumo: " + e.getMessage());
		}

		return deliveryDto;
	}

	public MicroserviceDeliveryDto disableDelivery(Long deliveryId) throws DisconnectedMicroserviceException {

		MicroserviceDeliveryDto deliveryDto = null;

		try {

			deliveryDto = operatorClient.disableDelivery(deliveryId);

		} catch (Exception e) {
			log.error("Error desactivando la entrega: " + e.getMessage());
		}

		return deliveryDto;
	}

	public MicroserviceDeliveryDto getDeliveryId(Long deliveryId) throws DisconnectedMicroserviceException {

		MicroserviceDeliveryDto deliveryDto = null;

		try {

			deliveryDto = operatorClient.findDeliveryById(deliveryId);

		} catch (Exception e) {
			log.error("Error consultando entrega: " + e.getMessage());
		}

		return deliveryDto;
	}

	public MicroserviceOperatorDto getOperatorById(Long operatorId) {

		MicroserviceOperatorDto operatorDto = null;

		try {

			operatorDto = operatorClient.findById(operatorId);

		} catch (Exception e) {
			log.error("Error consultando operador: " + e.getMessage());
		}

		return operatorDto;
	}

	public MicroserviceOperatorDto addUserToOperator(Long operatorId, Long userCode) {

		MicroserviceOperatorDto operatorDto = null;

		try {

			MicroserviceAddUserToOperatorDto requestAddUser = new MicroserviceAddUserToOperatorDto();
			requestAddUser.setOperatorId(operatorId);
			requestAddUser.setUserCode(userCode);

			operatorDto = operatorClient.addUserToOperator(requestAddUser);

		} catch (Exception e) {
			log.error("Error agregando usuario al operador: " + e.getMessage());
		}

		return operatorDto;
	}

	public List<MicroserviceOperatorUserDto> getUsersByOperator(Long operatorId) {

		List<MicroserviceOperatorUserDto> users = new ArrayList<>();

		try {

			users = operatorClient.getUsersByOperator(operatorId);

		} catch (Exception e) {
			log.error("Error consultando usuarios por operador: " + e.getMessage());
		}

		return users;
	}
	
	public MicroserviceOperatorDto addOperator(MicroserviceCreateOperatorDto operator) {
		MicroserviceOperatorDto operatorDto = null;
		try {
			operatorDto = operatorClient.addOperator(operator);
		} catch (Exception e) {
			log.error("No se ha podido agregar el operador: " + e.getMessage());
		}
		return operatorDto;
	}

	public MicroserviceOperatorDto activateOperator(Long operatorId) {
		MicroserviceOperatorDto operatorDto = null;
		try {
			operatorDto = operatorClient.activateOperator(operatorId);
		} catch (Exception e) {
			log.error("No se ha podido activar el operador: " + e.getMessage());
		}
		return operatorDto;
	}

	public MicroserviceOperatorDto deactivateOperator(Long operatorId) {
		MicroserviceOperatorDto operatorDto = null;
		try {
			operatorDto = operatorClient.deactivateOperator(operatorId);
		} catch (Exception e) {
			log.error("No se ha podido desactivar el operador: " + e.getMessage());
		}
		return operatorDto;
	}
	
	public MicroserviceOperatorDto updateOperator(MicroserviceUpdateOperatorDto operator) {
		MicroserviceOperatorDto operatorDto = null;
		try {
			operatorDto = operatorClient.updateOperator(operator);
		} catch (Exception e) {
			log.error("No se ha podido actualizar el operador: " + e.getMessage());
		}
		return operatorDto;
	}

}
