package com.ai.st.microservice.workspaces.business;

import org.springframework.stereotype.Component;

@Component
public class RoleBusiness {

	public static final Long ROLE_ADMINISTRATOR = (long) 1; // ADMINISTRADOR
	public static final Long ROLE_MANAGER = (long) 2; // GESTOR
	public static final Long ROLE_OPERATOR = (long) 3; // OPERADOR
	public static final Long ROLE_SUPPLY_SUPPLIER = (long) 4; // PROVEEDOR INSUMO

}
