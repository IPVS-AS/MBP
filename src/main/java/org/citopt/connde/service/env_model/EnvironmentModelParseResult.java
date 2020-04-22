package org.citopt.connde.service.env_model;

import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.springframework.validation.Errors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EnvironmentModelParseResult {
    private Set<Device> deviceSet;
    private Set<Component> componentSet;
    private Map<Component, Device> connections;
    private Map<UserEntity, Errors> errors;

    public EnvironmentModelParseResult() {
        this.deviceSet = new HashSet<>();
        this.componentSet = new HashSet<>();
        this.connections = new HashMap<>();
        this.errors = new HashMap<>();
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void addDevice(Device device) {
        //Sanity check
        if (device == null) {
            throw new IllegalArgumentException("Device must not be null.");
        }

        this.deviceSet.add(device);
    }

    public void removeDevice(Device device) {
        //Sanity check
        if (device == null) {
            throw new IllegalArgumentException("Device must not be null.");
        }

        this.deviceSet.remove(device);
    }

    public void addComponent(Component component) {
        //Sanity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        this.componentSet.add(component);
    }

    public void removeComponent(Component component) {
        //Sanity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        this.componentSet.remove(component);
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

    public Set<Device> getDeviceSet() {
        return deviceSet;
    }

    public void setDeviceSet(Set<Device> deviceSet) {
        this.deviceSet = deviceSet;
    }

    public Set<Component> getComponentSet() {
        return componentSet;
    }

    public void setComponentSet(Set<Component> componentSet) {
        this.componentSet = componentSet;
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
