package com.ai.st.microservice.workspaces.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.st.microservice.workspaces.clients.ReportFeignClient;
import com.ai.st.microservice.workspaces.dto.reports.MicroserviceReportInformationDto;
import com.ai.st.microservice.workspaces.dto.reports.MicroserviceRequestReportDownloadSupplyIndividualDto;
import com.ai.st.microservice.workspaces.dto.reports.MicroserviceRequestReportDownloadSupplyTotalDto;
import com.ai.st.microservice.workspaces.exceptions.BusinessException;

@Service
public class ReportBusiness {

	private final Logger log = LoggerFactory.getLogger(ReportBusiness.class);

	@Autowired
	private ReportFeignClient reportClient;

	public MicroserviceReportInformationDto generateReportDownloadSupplyIndividual(String title, String namespace)
			throws BusinessException {

		MicroserviceReportInformationDto informationDto = null;

		try {

			MicroserviceRequestReportDownloadSupplyIndividualDto data = new MicroserviceRequestReportDownloadSupplyIndividualDto();
			data.setTitle(title);
			data.setNamespace(namespace);

			informationDto = reportClient.createReportDownloadSuppliesIndiviual(data);
		} catch (Exception e) {
			log.error("Error creando reporte (entrega de insumos por parte del gestor al operador individual): "
					+ e.getMessage());
			throw new BusinessException("No se ha podido generar el reporte.");
		}

		return informationDto;
	}

	public MicroserviceReportInformationDto generateReportDownloadSupplyTotal(String title, String namespace)
			throws BusinessException {

		MicroserviceReportInformationDto informationDto = null;

		try {

			MicroserviceRequestReportDownloadSupplyTotalDto data = new MicroserviceRequestReportDownloadSupplyTotalDto();
			data.setTitle(title);
			data.setNamespace(namespace);

			informationDto = reportClient.createReportDownloadSuppliesTotal(data);
		} catch (Exception e) {
			log.error("Error creando reporte (entrega de insumos por parte del gestor al operador total): "
					+ e.getMessage());
			throw new BusinessException("No se ha podido generar el reporte.");
		}

		return informationDto;
	}

}
