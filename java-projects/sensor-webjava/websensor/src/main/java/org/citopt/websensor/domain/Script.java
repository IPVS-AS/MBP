package org.citopt.websensor.domain;

import java.util.Objects;
import javax.persistence.GeneratedValue;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.multipart.MultipartFile;

@Document
public class Script {

    @Id
    @GeneratedValue
    private ObjectId id;

    @Indexed(unique = true)
    private String name;
    
    private String description;

    private MultipartFile service;
    private MultipartFile routine;

    @PersistenceConstructor
    public Script(ObjectId id, String name, MultipartFile service, MultipartFile routine) {
        this.id = id;
        this.name = name;
        this.service = service;
        this.routine = routine;
    }

    public Script() {
    }

    public Script(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }    

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MultipartFile getService() {
        return service;
    }

    public void setService(MultipartFile service) {
        this.service = service;
    }

    public MultipartFile getRoutine() {
        return routine;
    }

    public void setRoutine(MultipartFile routine) {
        this.routine = routine;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.id);
        hash = 29 * hash + Objects.hashCode(this.name);
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
        final Script other = (Script) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Script{" + "id=" + id + ", name=" + name + ", service=" + service + ", routine=" + routine + '}';
    }

}
