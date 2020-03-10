package com.ai.st.microservice.workspaces.dto.supplies;

import java.io.Serializable;

public class MicroserviceSupplyStateDto implements Serializable {

	private static final long serialVersionUID = 5317228706593217348L;

	private Long id;
	private String name;

	public MicroserviceSupplyStateDto() {

	}

	public MicroserviceSupplyStateDto(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
