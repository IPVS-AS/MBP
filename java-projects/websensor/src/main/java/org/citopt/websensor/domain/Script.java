package org.citopt.websensor.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.GeneratedValue;
import org.bson.types.ObjectId;
import org.citopt.websensor.web.exception.NotFoundException;
import org.citopt.websensor.dao.InsertFailureException;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Script {
    
    @Id
    @GeneratedValue
    private ObjectId id;

    @Indexed(unique = true)
    private String name;
    
    private String description;

    private ScriptFile service;
    private List<ScriptFile> routines;

    @PersistenceConstructor
    public Script(ObjectId id, String name, ScriptFile service, List<ScriptFile> routines) {
        this.id = id;
        this.name = name;
        this.service = service;
        this.routines = routines;
    }

    public Script() {
        this.routines = new ArrayList<>();
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

    public ScriptFile getService() {
        return service;
    }

    public void setService(ScriptFile service) {
        this.service = service;
    }

    public List<ScriptFile> getRoutines() {
        return routines;
    }

    public void addRoutine(ScriptFile routine) throws InsertFailureException {
        for(ScriptFile item : this.routines) {
            if(routine.getName().equals(item.getName())) {
                throw new InsertFailureException("Filename cannot be duplicate!");
            }
        }
        this.routines.add(routine);
    }
    
    public ScriptFile getRoutine(String filename) throws NotFoundException {
        for(ScriptFile item : this.routines) {
            if(item.getName().equals(filename)) {
                return item;
            }
        }        
        throw new NotFoundException("No such filename.");
    }
    
    public void deleteRoutine(String filename) throws NotFoundException {
        for(ScriptFile item : this.routines) {
            if(item.getName().equals(filename)) {
               this.routines.remove(item);
            }
        }        
        throw new NotFoundException("No such filename.");
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
        return "Script{" + "id=" + id + ", name=" + name + ", service=" + service + ", routine=" + routines + '}';
    }

}
