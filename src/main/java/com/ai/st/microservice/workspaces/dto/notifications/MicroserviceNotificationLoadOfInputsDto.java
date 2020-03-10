package com.ai.st.microservice.workspaces.dto.notifications;

import java.io.Serializable;
import java.util.Date;

public class MicroserviceNotificationLoadOfInputsDto implements Serializable {

	private static final long serialVersionUID = 3050930525504636650L;

	private Long userCode;
	private String email;
	private String type;
	private int status;
	private boolean loadStatus;
	private String mpio;
	private String dpto;
	private String requestNumber;
	private Date loadDate;
	private String supportFile;

	public MicroserviceNotificationLoadOfInputsDto() {

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

	public String getSupportFile() {
		return supportFile;
	}

	public void setSupportFile(String supportFile) {
		this.supportFile = supportFile;
	}

	public boolean isLoadStatus() {
		return loadStatus;
	}

	public void setLoadStatus(boolean loadStatus) {
		this.loadStatus = loadStatus;
	}

	public String getRequestNumber() {
		return requestNumber;
	}

	public void setRequestNumber(String requestNumber) {
		this.requestNumber = requestNumber;
	}

	public Date getLoadDate() {
		return loadDate;
	}

	public void setLoadDate(Date loadDate) {
		this.loadDate = loadDate;
	}

	public String getSubject() {
		return "Notificación Sistema de Transición Barrido Predial – Cargue de Insumo XTF";
	}

	public String getBody() {
		String msgStatus = "ha sido validado y cargado SATISFACTORIAMENTE.";
		if (!this.loadStatus) {
			msgStatus = "ha sido validado y ha FALLADO en su carga, por lo cual se solicita volver a subirlo al sistema.";
		}
		String html = "";
		html += "<div>El Sistema De Transición para el Barrido Predial en Colombia le informa:</div>";
		html += "<div>Que el insumo “" + this.supportFile + "” del municipio de " + this.mpio + " del departamento de "
				+ this.dpto + " con el número de solicitud " + this.requestNumber + " cargado el "
				+ this.loadDate.toString() + ", " + msgStatus + "</div>";
		html += "<div>Para mayor detalle por favor diríjase al Sistema de Transición en la siguiente URL e ingrese con su respectivo usuario y contraseña que le ha sido asignada previamente.</div>";
		html += "<div><a href='https://st.proadmintierra.info/login'>https://st.proadmintierra.info/login</a></div>";
		html += "<div>Nota: Cualquier inquietud o inconveniente en el ingreso a la plataforma por favor comunicarse con el siguiente correo: soporte_ST@proadmintierra.info</div>";
		html += "<div>--</div>";
		html += "SISTEMA DE TRANSICIÓN</div>";
		return html;

	}

}
