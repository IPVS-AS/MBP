/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.device;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;

@Entity("devices")
@Indexes(
        @Index(value = "device", fields = @Field("device"))
)
/**
 *
 * @author rafaelkperes
 */
public class Sensor {

    @Id
    private ObjectId id;

    @Reference
    private Device device;

    @Reference
    private List<Pin> pinSet;

    @Reference
    private Script script;

    private Status status;
    
    @Transient
    private NetworkStatus networkStatus;

    public enum Status {
        ACTIVE,
        INACTIVE
    }

    public enum NetworkStatus {
        REACHABLE,
        UNREACHABLE,
    }

    protected Sensor() {
        this.networkStatus = NetworkStatus.UNREACHABLE;
    }
    
    public Sensor(Device device) {
        this.device = device;
        this.pinSet = new LinkedList<>();
        this.status = Status.INACTIVE;
        this.networkStatus = NetworkStatus.UNREACHABLE;
    }

    public Sensor(Device device, List<Pin> pinSet, Script script) {
        this.device = device;
        this.pinSet = pinSet;
        this.script = script;
        this.status = Status.INACTIVE;
        this.networkStatus = NetworkStatus.UNREACHABLE;
    }

    public Sensor(Device device, List<Pin> pinSet, Script script, Status status) {
        this(device, pinSet, script);
        this.status = status;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public void addPin(Pin p) {
        this.pinSet.add(p);
    }

    public List<Pin> getPinSet() {
        return pinSet;
    }

    public void setPinSet(List<Pin> pinSet) {
        this.pinSet = pinSet;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public NetworkStatus getNetworkStatus() {
        return networkStatus;
    }

    public void setNetworkStatus(NetworkStatus networkStatus) {
        if (Status.INACTIVE.equals(this.status)) {
            throw new IllegalArgumentException();
        }
        this.networkStatus = networkStatus;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.id);
        hash = 31 * hash + Objects.hashCode(this.device);
        hash = 31 * hash + Objects.hashCode(this.pinSet);
        hash = 31 * hash + Objects.hashCode(this.script);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Sensor other = (Sensor) obj;
        if (!Objects.equals(this.device, other.device)) {
            return false;
        }
        if (!Objects.equals(this.pinSet, other.pinSet)) {
            return false;
        }
        if (!Objects.equals(this.script, other.script)) {
            return false;
        }
        return true;
    }
}
