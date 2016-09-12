package org.citopt.websensor.domain;

import java.util.Objects;
import javax.persistence.GeneratedValue;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Reference;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Device {
    
    @Id
    @GeneratedValue
    private ObjectId id;
    
    @Indexed(unique = true)
    private String macAddress;
    
    @Reference
    private Location location;

    @PersistenceConstructor
    public Device(ObjectId id, String macAddress, Location location) {
        this.id = id;
        this.macAddress = macAddress;
        this.location = location;
    }
    
    public Device() {
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
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
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Device{" + "id=" + id + ", macAddress=" + macAddress + ", location=" + location + '}';
    }
    
}
