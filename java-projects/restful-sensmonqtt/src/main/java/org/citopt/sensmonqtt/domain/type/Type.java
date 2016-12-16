package org.citopt.sensmonqtt.domain.type;

import org.citopt.sensmonqtt.exception.InsertFailureException;
import org.citopt.sensmonqtt.exception.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.GeneratedValue;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author rafaelkperes
 */
@Document
public class Type {

    @Id
    @GeneratedValue
    private ObjectId id;

    @Indexed(unique = true)
    private String name;

    private String description;

    private Code service;
    private List<Code> routines;

    public Type() {
        this.routines = new ArrayList<>();
    }

    public List<Code> getRoutines() {
        return routines;
    }

    public void addRoutine(Code routine) throws InsertFailureException {
        for (Code item : this.routines) {
            if (routine.getName().equals(item.getName())) {
                throw new InsertFailureException("Filename cannot be duplicate!");
            }
        }
        this.routines.add(routine);
    }

    public Code getRoutine(String filename) throws NotFoundException {
        for (Code item : this.routines) {
            if (item.getName().equals(filename)) {
                return item;
            }
        }
        throw new NotFoundException("No such filename.");
    }

    public void deleteRoutine(String filename) throws NotFoundException {
        for (Code item : this.routines) {
            System.out.println(item.getName());
            if (item.getName().equals(filename)) {
                this.routines.remove(item);
                return;
            }
        }
        throw new NotFoundException("No such filename.");
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

    public Code getService() {
        return service;
    }

    public void setService(Code service) {
        this.service = service;
    }

}
