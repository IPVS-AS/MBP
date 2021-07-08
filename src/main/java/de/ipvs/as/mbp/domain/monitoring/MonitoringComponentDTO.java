package de.ipvs.as.mbp.domain.monitoring;

public class MonitoringComponentDTO {
	
	private String id;
	private String name;
	private String monitoringOperatorId;
	private String deviceId;

	public MonitoringComponentDTO(MonitoringComponent monitoringComponent) {
		this.id = monitoringComponent.getId();
		this.name = monitoringComponent.getName();
		this.monitoringOperatorId = monitoringComponent.getOperator().getId();
		this.deviceId = monitoringComponent.getDevice().getId();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMonitoringOperatorId() {
		return monitoringOperatorId;
	}

	public void setMonitoringOperatorId(String monitoringOperatorId) {
		this.monitoringOperatorId = monitoringOperatorId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
}
