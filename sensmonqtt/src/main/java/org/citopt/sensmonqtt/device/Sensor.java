/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.Reference;

@Entity("devices")
    @Indexes(
            @Index(value = "macAddress", fields = @Field("macAddress"))
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
    private Set<Pin> pinSet;
    
    @Reference
    private Type type;
    
    @Reference
    private Status status;
    
    @Reference
    private NetworkStatus networkStatus;

    @Entity
    public enum Status {
        ACTIVE,
        INACTIVE
    }
    
    @Entity
    public enum NetworkStatus {
        REACHABLE,
        UNREACHABLE,
    }
    
    protected Sensor() {
    }
    
    /**
     * Status set as INACTIVE.
     * Network Status will always start as UNREACHABLE.
     * @param macAddress Device MAC Address
     * @param pinSet List of pins used to read values on device
     * @param type Type of the device will determine which script will be deployed
     */
    public Sensor(Device device, Set<Pin> pinSet, Type type) {
        this.device = device;
        this.pinSet = pinSet;
        this.type = type;
        this.status = Status.INACTIVE;
        this.networkStatus = NetworkStatus.UNREACHABLE;
        
        //this.id = Device.createId(this.macAddress, this.pinSet);
    }
    
    /**
     * Network Status will always start as UNREACHABLE.
     * @param macAddress Device MAC Address
     * @param pinSet List of pins used to read values on device
     * @param type Type of the device will determine which script will be deployed
     * @param status Initial status of the Device
     */
    public Sensor(Device device, Set<Pin> pinSet, Type type, Status status) {
        this(device, pinSet, type);
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
    
    public Set<Pin> getPinSet() {
        return pinSet;
    }

    public void setPinSet(Set<Pin> pinSet) {
        this.pinSet = pinSet;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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
        hash = 31 * hash + Objects.hashCode(this.type);
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
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.device, other.device)) {
            return false;
        }
        if (!Objects.equals(this.pinSet, other.pinSet)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        return true;
    }
}
