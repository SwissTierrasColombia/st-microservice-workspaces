package com.ai.st.microservice.workspaces.dto.notifications;

import java.io.Serializable;
import java.util.Date;

public class MicroserviceNotificationInputIntegrationsDto implements Serializable {

	private static final long serialVersionUID = 3050930525504636650L;

	private Long userCode;
	private String email;
	private String type;
	private int status;
	private String integrationStatus;
	private String mpio;
	private String dpto;
	private Date integrationDate;

	public MicroserviceNotificationInputIntegrationsDto() {

	}

	public Long getUserCode() {
		return userCode;
	}

	public void setUserCode(Long userCode) {
		this.userCode = userCode;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMpio() {
		return mpio;
	}

	public void setMpio(String mpio) {
		this.mpio = mpio;
	}

	public String getDpto() {
		return dpto;
	}

	public void setDpto(String dpto) {
		this.dpto = dpto;
	}

	public String getIntegrationStatus() {
		return integrationStatus;
	}

	public void setIntegrationStatus(String integrationStatus) {
		this.integrationStatus = integrationStatus;
	}

	public Date getIntegrationDate() {
		return integrationDate;
	}

	public void setIntegrationDate(Date integrationDate) {
		this.integrationDate = integrationDate;
	}

	public String getSubject() {
		return "Notificación Sistema de Transición Barrido Predial – Integración de Insumos XTF";
	}

	public String getBody() {
		String html = "";
		html += "<div>El Sistema De Transición para el Barrido Predial en Colombia le informa:</div>";
		html += "<div>Que la integración de los archivos XTF del municipio de " + this.mpio + " del departamento de "
				+ this.dpto + " solicitada el " + this.integrationDate.toString() + ", se encuentra en estado "
				+ this.integrationStatus + ".</div>";
		html += "<div>Para mayor detalle por favor diríjase al Sistema de Transición en la siguiente URL e ingrese con su respectivo usuario y contraseña que le ha sido asignada previamente.</div>";
		html += "<div><a href='https://st.proadmintierra.info/login'>https://st.proadmintierra.info/login</a></div>";
		html += "<div>Nota: Cualquier inquietud o inconveniente en el ingreso a la plataforma por favor comunicarse con el siguiente correo: soporte_ST@proadmintierra.info</div>";
		html += "<div>--</div>";
		html += "SISTEMA DE TRANSICIÓN</div>";
		return html;

	}

}
