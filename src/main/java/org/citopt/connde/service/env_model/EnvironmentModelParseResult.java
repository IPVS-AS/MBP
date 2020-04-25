package org.citopt.connde.service.env_model;

import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.springframework.validation.Errors;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentModelParseResult {
    //(node id -> component)
    private Map<String, Device> deviceMap;
    //(node id -> component)
    private Map<String, Component> componentMap;
    private Map<Component, Device> connections;
    private Map<UserEntity, Errors> errors;

    public EnvironmentModelParseResult() {
        this.deviceMap = new HashMap<>();
        this.componentMap = new HashMap<>();
        this.connections = new HashMap<>();
        this.errors = new HashMap<>();
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void addDevice(Device device, String nodeId) {
        //Sanity check
        if (device == null) {
            throw new IllegalArgumentException("Device must not be null.");
        } else if ((nodeId == null) || (nodeId.isEmpty())) {
            throw new IllegalArgumentException("Node ID must not be null or empty.");
        }

        this.deviceMap.put(nodeId, device);
    }

    public void removeDevice(String nodeId) {
        //Sanity check
        if ((nodeId == null) || (nodeId.isEmpty())) {
            throw new IllegalArgumentException("Node ID must not be null.");
        }

        this.deviceMap.remove(nodeId);
    }

    public void addComponent(Component component, String nodeId) {
        //Sanity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        } else if ((nodeId == null) || (nodeId.isEmpty())) {
            throw new IllegalArgumentException("Node ID must not be null or empty.");
        }

        this.componentMap.put(nodeId, component);
    }

    public void removeComponent(String nodeId) {
        //Sanity check
        if ((nodeId == null) || nodeId.isEmpty()) {
            throw new IllegalArgumentException("Node ID must not be null or empty.");
        }

        this.componentMap.remove(nodeId);
    }

    public void addConnection(Device device, Component component) {
        //Sanity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        } else if (device == null) {
            throw new IllegalArgumentException("Device must not be null.");
        }

        this.connections.put(component, device);
    }

    public void addErrors(UserEntity entity, Errors errors) {
        //Sanity check
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null.");
        } else if (errors == null) {
            throw new IllegalArgumentException("Errors must not be null.");
        }

        this.errors.put(entity, errors);
    }

    public Map<String, Device> getDeviceMap() {
        return deviceMap;
    }

    public void setDeviceMap(Map<String, Device> deviceMap) {
        this.deviceMap = deviceMap;
    }

    public Map<String, Component> getComponentMap() {
        return componentMap;
    }

    public void setComponentMap(Map<String, Component> componentMap) {
        this.componentMap = componentMap;
    }

    public Map<Component, Device> getConnections() {
        return connections;
    }

    public void setConnections(Map<Component, Device> connections) {
        this.connections = connections;
    }

    public Map<UserEntity, Errors> getErrors() {
        return errors;
    }

    public void setErrors(Map<UserEntity, Errors> errors) {
        this.errors = errors;
    }
}
