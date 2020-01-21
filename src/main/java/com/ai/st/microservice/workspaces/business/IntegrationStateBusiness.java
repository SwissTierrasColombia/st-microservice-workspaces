package com.ai.st.microservice.workspaces.business;

import org.springframework.stereotype.Component;

@Component
public class IntegrationStateBusiness {

	public static final Long STATE_STARTED_AUTOMATIC = (long) 1;
	public static final Long STATE_FINISHED_AUTOMATIC = (long) 2;
	public static final Long STATE_STARTED_ASSISTED = (long) 3;
	public static final Long STATE_FINISHED_ASSISTED = (long) 4;
	public static final Long STATE_PRODUCT_GENERATED = (long) 5;
	public static final Long STATE_ERROR = (long) 6;

}
