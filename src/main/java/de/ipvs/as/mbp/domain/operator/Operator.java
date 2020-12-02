package de.ipvs.as.mbp.domain.operator;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;
import javax.persistence.GeneratedValue;

import de.ipvs.as.mbp.domain.operator.parameters.Parameter;
import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Document class for operators.
 */
@Document
public class Operator extends UserEntity {

    @Id
    @GeneratedValue
    private String id;

    private String name;

    private String description;

    private String unit;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Code> routines;

    private List<Parameter> parameters;

    public Operator() {
        this.routines = new ArrayList<>();
        this.parameters = new ArrayList<>();
    }

    public List<Code> getRoutines() {
        return routines;
    }

    public void addRoutine(Code routine) {
        this.routines.add(routine);
    }

    public boolean hasRoutines() {
        return !this.routines.isEmpty();
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @JsonIgnore
    public Unit<? extends Quantity> getUnitObject() {
        try {
            return Unit.valueOf(this.unit);
        } catch (Exception e) {
            return null;
        }
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }
}