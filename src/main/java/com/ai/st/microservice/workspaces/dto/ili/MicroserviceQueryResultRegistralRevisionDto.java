package com.ai.st.microservice.workspaces.dto.ili;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MicroserviceQueryResultRegistralRevisionDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private int currentPage;
	private int totalPages;
	private List<MicroserviceItemRegistralRevisionDto> records;

	public MicroserviceQueryResultRegistralRevisionDto() {
		this.records = new ArrayList<>();
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public List<MicroserviceItemRegistralRevisionDto> getRecords() {
		return records;
	}

	public void setRecords(List<MicroserviceItemRegistralRevisionDto> records) {
		this.records = records;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

}
