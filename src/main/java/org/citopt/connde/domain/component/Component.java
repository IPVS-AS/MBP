package org.citopt.connde.domain.component;

import javax.persistence.GeneratedValue;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.adapter.Adapter;
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
    private Adapter adapter;
    
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
    public Adapter getAdapter() {
        return adapter;
    }

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device address) {
        this.device = address;
    }

    public String getTopicName(){
        return getComponentTypeName() + "/" + id;
    }

    public abstract String getComponentTypeName();

    @Override
    public String toString() {
        return "Component{" + "id=" + id + ", name=" + name + ", type=" + adapter + '}';
    }
}
