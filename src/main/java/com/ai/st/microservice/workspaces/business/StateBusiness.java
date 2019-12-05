package com.ai.st.microservice.workspaces.business;

import org.springframework.stereotype.Component;

@Component
public class StateBusiness {

	public static final Long STATE_START = (long) 1;
	public static final Long STATE_SUPPLIES_REQUESTED = (long) 2;
	public static final Long STATE_SUPPLIES_RECEIVED = (long) 3;
	public static final Long STATE_SUPPLIES_INTEGRATED = (long) 4;
	public static final Long STATE_SUPPLIES_DELIVERED = (long) 5;
	public static final Long STATE_SUPPLIES_DOWNLOADED = (long) 6;

}
