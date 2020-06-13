package org.citopt.connde.service.env_model.events.types;

public class ComponentValueEvent extends EnvironmentModelEvent {

    //Name of the event
    private static final String EVENT_NAME = "component_value";

    private String nodeId;
    private String unit;
    private double value;

    public ComponentValueEvent(String nodeId, String unit, double value) {
        this.nodeId = nodeId;
        this.unit = unit;
        this.value = value;
    }

    /**
     * Returns the name of the event, allowing to identify and recognize its type.
     *
     * @return The name of the event
     */
    @Override
    public String getName() {
        return EVENT_NAME;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
