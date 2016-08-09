/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.device;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.Reference;

/**
 *
 * @author rafaelkperes
 */
@Entity("devices")
@Indexes({
    @Index(fields = {
        @Field("macAddress")},
            options = @IndexOptions(unique = true))
})
public class Device {

    @Id
    private ObjectId id;

    private String macAddress;

    @Reference
    private Set<Pin> availablePins;

    @Reference
    private Set<Pin> allocatedPins;

    @Reference
    private Location location;

    protected Device() {
        this.availablePins = new HashSet<>();
        this.allocatedPins = new HashSet<>();
    }

    public Device(String macAddress) {
        macAddress = macAddress.replace(" ", "");
        macAddress = macAddress.replace(":", "");
        macAddress = macAddress.replace("-", "");
        this.macAddress = macAddress.toLowerCase();
        this.availablePins = new HashSet<>();
        this.allocatedPins = new HashSet<>();
    }

    public Device(String macAddress, Location location) {
        this.macAddress = macAddress;
        this.location = location;
        this.availablePins = new HashSet<>();
        this.allocatedPins = new HashSet<>();
    }

    public ObjectId getId() {
        return id;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public Set<Pin> getAvailablePins() {
        return availablePins;
    }

    public Set<Pin> getAllocatedPins() {
        return allocatedPins;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void addPin(Pin p) {
        this.availablePins.add(p);
    }

    public void allocatePin(Pin p) {
        if (!this.availablePins.contains(p)) {
            throw new IllegalArgumentException("pin already allocated");
        }
        this.availablePins.remove(p);
        this.allocatedPins.add(p);
    }

    public boolean isAvailable(Pin p) {
        return this.availablePins.contains(p);
    }

    public boolean isAllocated(Pin p) {
        return this.allocatedPins.contains(p);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.id);
        hash = 47 * hash + Objects.hashCode(this.macAddress);
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
        final Device other = (Device) obj;
        if (!Objects.equals(this.macAddress, other.macAddress)) {
            return false;
        }
        return true;
    }

}
