package com.ai.st.microservice.workspaces.dto.ili;

import java.io.Serializable;

public class MicroserviceIntegrationStatDto implements Serializable {

	private static final long serialVersionUID = 6963849125361470575L;

	private long countSNR;
	private long countGC;
	private long countMatch;
	private double percentage;
	private boolean status;

	public MicroserviceIntegrationStatDto() {
		this.countGC = 0;
		this.countSNR = 0;
		this.countMatch = 0;
		this.percentage = 0.0;
	}

	public long getCountSNR() {
		return countSNR;
	}

	public void setCountSNR(long countSNR) {
		this.countSNR = countSNR;
	}

	public long getCountGC() {
		return countGC;
	}

	public void setCountGC(long countGC) {
		this.countGC = countGC;
	}

	public double getPercentage() {
		return percentage;
	}

	public void setPercentage(double percentage) {
		this.percentage = percentage;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public long getCountMatch() {
		return countMatch;
	}

	public void setCountMatch(long countMatch) {
		this.countMatch = countMatch;
	}

}
