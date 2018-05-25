package org.citopt.connde.domain.component;

import javax.persistence.GeneratedValue;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.type.Type;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author rafaelkperes
 */
@Document
public abstract class Component {
    
    @Id
    @GeneratedValue
    private String id;

    @Indexed(unique = true)
    private String name;
    
    @Reference
    private Type type;
    
    @Reference
    private Device device;

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
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device address) {
        this.device = address;
    }

    @Override
    public String toString() {
        return "Component{" + "id=" + id + ", name=" + name + ", type=" + type + '}';
    }

}
