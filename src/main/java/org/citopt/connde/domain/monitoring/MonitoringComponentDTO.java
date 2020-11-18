package org.citopt.connde.domain.monitoring;

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

	public String getName() {
		return name;
	}

	public String getMonitoringOperatorId() {
		return monitoringOperatorId;
	}

	public String getDeviceId() {
		return deviceId;
	}
}
