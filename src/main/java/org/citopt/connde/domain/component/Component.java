package org.citopt.connde.domain.component;

import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.citopt.connde.domain.user_entity.UserEntityPolicy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;

import java.util.Objects;

import static org.citopt.connde.domain.user_entity.UserEntityRole.ADMIN;
import static org.citopt.connde.domain.user_entity.UserEntityRole.APPROVED_USER;

/**
 * Document super class for components (actuators, sensors, ...).
 */
@Document
public abstract class Component extends UserEntity {
    //Permission name for deployment
    private static final String PERMISSION_NAME_DEPLOY = "deploy";

    //Extend default policy by deployment permission
    private static final UserEntityPolicy COMPONENT_POLICY = new UserEntityPolicy(DEFAULT_POLICY)
            .addPermission(PERMISSION_NAME_DEPLOY).addRole(APPROVED_USER).addRole(ADMIN).lock();

    @Id
    @GeneratedValue
    private String id;

    @Indexed(unique = true)
    private String name;

    @Indexed
    private String componentType;

    @DBRef
    private Adapter adapter;

    @DBRef
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

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
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

    public String getTopicName() {
        return getComponentTypeName() + "/" + id;
    }

    public abstract String getComponentTypeName();

    @Override
    public String toString() {
        return "Component{" + "id=" + id + ", name=" + name + ", type=" + adapter + '}';
    }


    @Override
    public UserEntityPolicy getUserEntityPolicy() {
        return COMPONENT_POLICY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Component component = (Component) o;
        return id.equals(component.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
