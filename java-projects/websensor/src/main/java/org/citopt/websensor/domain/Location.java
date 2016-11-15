package org.citopt.websensor.domain;

import java.util.Objects;
import javax.persistence.GeneratedValue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Location {

    @Id
    @GeneratedValue
    private ObjectId id;

    @NotNull
    @Size(min = 2, max=32)
    @Indexed(unique = true)
    private String name;

    private String description;

    @PersistenceConstructor
    protected Location(ObjectId id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Location() {
    }

    public Location(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public ObjectId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.id);
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + Objects.hashCode(this.description);
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
        final Location other = (Location) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Location{" + "id=" + id + ", name=" + name + ", description=" + description + '}';
    }

}
