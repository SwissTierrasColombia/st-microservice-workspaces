package com.ai.st.microservice.workspaces.business;

import org.springframework.stereotype.Component;

@Component
public class RoleBusiness {

	public static final Long ROLE_ADMINISTRATOR = (long) 1;
	public static final Long ROLE_MANAGER = (long) 2;
	public static final Long ROLE_OPERATOR = (long) 3;
	public static final Long ROLE_SUPPLY_SUPPLIER = (long) 4;
	public static final Long ROLE_SUPER_ADMINISTRATOR = (long) 5;

	public static final Long SUB_ROLE_DIRECTOR = (long) 1;
	public static final Long SUB_ROLE_INTEGRATOR = (long) 2;

	public static final Long SUB_ROLE_DIRECTOR_PROVIDER = (long) 1;
	public static final Long SUB_ROLE_DELEGATE_PROVIDER = (long) 2;

}
