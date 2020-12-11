package com.ai.st.microservice.workspaces.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ai.st.microservice.workspaces.dto.supplies.MicroserviceSupplyDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "IntegrationDto", description = "Integration Dto")
public class IntegrationDto implements Serializable {

	private static final long serialVersionUID = -2795942533700928564L;

	@ApiModelProperty(required = true, notes = "Integration ID")
	private Long id;

	@ApiModelProperty(required = true, notes = "Started at")
	private Date startedAt;

	@ApiModelProperty(required = true, notes = "Finished at")
	private Date finishedAt;

	@ApiModelProperty(required = true, notes = "Integration state")
	private IntegrationStateDto integrationState;

	@ApiModelProperty(required = true, notes = "Hostname (crypto)")
	private String hostname;

	@ApiModelProperty(required = true, notes = "Port (crypto)")
	private String port;

	@ApiModelProperty(required = true, notes = "Database (crypto)")
	private String database;

	@ApiModelProperty(required = true, notes = "Schema (crypto)")
	private String schema;

	@ApiModelProperty(required = true, notes = "Username (crypto)")
	private String username;

	@ApiModelProperty(required = true, notes = "Password (crypto)")
	private String password;

	@ApiModelProperty(required = true, notes = "Supply Cadastre ID")
	private Long supplyCadastreId;

	@ApiModelProperty(required = true, notes = "Supply SNR ID")
	private Long supplySnrId;

	@ApiModelProperty(required = true, notes = "Supply ANT ID")
	private Long supplyAntId;

	@ApiModelProperty(required = true, notes = "Supply Cadastre")
	private MicroserviceSupplyDto supplyCadastre;

	@ApiModelProperty(required = true, notes = "Supply Snr")
	private MicroserviceSupplyDto supplySnr;

	@ApiModelProperty(required = true, notes = "Supply Ant")
	private MicroserviceSupplyDto supplyAnt;

	@ApiModelProperty(required = true, notes = "Stats")
	private List<IntegrationStatDto> stats;

	@ApiModelProperty(required = true, notes = "Histories")
	private List<IntegrationHistoryDto> histories;

	@ApiModelProperty(required = true, notes = "Municipality")
	private MunicipalityDto municipalityDto;

	@ApiModelProperty(required = true, notes = "URL Map")
	private String urlMap;

	public IntegrationDto() {
		this.stats = new ArrayList<>();
		this.histories = new ArrayList<>();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}

	public Date getFinishedAt() {
		return finishedAt;
	}

	public void setFinishedAt(Date finishedAt) {
		this.finishedAt = finishedAt;
	}

	public IntegrationStateDto getIntegrationState() {
		return integrationState;
	}

	public void setIntegrationState(IntegrationStateDto integrationState) {
		this.integrationState = integrationState;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<IntegrationStatDto> getStats() {
		return stats;
	}

	public void setStats(List<IntegrationStatDto> stats) {
		this.stats = stats;
	}

	public Long getSupplyCadastreId() {
		return supplyCadastreId;
	}

	public void setSupplyCadastreId(Long supplyCadastreId) {
		this.supplyCadastreId = supplyCadastreId;
	}

	public Long getSupplySnrId() {
		return supplySnrId;
	}

	public void setSupplySnrId(Long supplySnrId) {
		this.supplySnrId = supplySnrId;
	}

	public Long getSupplyAntId() {
		return supplyAntId;
	}

	public void setSupplyAntId(Long supplyAntId) {
		this.supplyAntId = supplyAntId;
	}

	public MicroserviceSupplyDto getSupplyCadastre() {
		return supplyCadastre;
	}

	public void setSupplyCadastre(MicroserviceSupplyDto supplyCadastre) {
		this.supplyCadastre = supplyCadastre;
	}

	public MicroserviceSupplyDto getSupplySnr() {
		return supplySnr;
	}

	public void setSupplySnr(MicroserviceSupplyDto supplySnr) {
		this.supplySnr = supplySnr;
	}

	public MicroserviceSupplyDto getSupplyAnt() {
		return supplyAnt;
	}

	public void setSupplyAnt(MicroserviceSupplyDto supplyAnt) {
		this.supplyAnt = supplyAnt;
	}

	public List<IntegrationHistoryDto> getHistories() {
		return histories;
	}

	public void setHistories(List<IntegrationHistoryDto> histories) {
		this.histories = histories;
	}

	public MunicipalityDto getMunicipalityDto() {
		return municipalityDto;
	}

	public void setMunicipalityDto(MunicipalityDto municipalityDto) {
		this.municipalityDto = municipalityDto;
	}

	public String getUrlMap() {
		return urlMap;
	}

	public void setUrlMap(String urlMap) {
		this.urlMap = urlMap;
	}

}
