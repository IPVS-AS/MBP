package de.ipvs.as.mbp.domain.operator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import javax.measure.unit.Unit;
import javax.persistence.GeneratedValue;
import de.ipvs.as.mbp.domain.data_model.DataModel;
import de.ipvs.as.mbp.domain.operator.parameters.Parameter;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import javax.measure.quantity.Quantity;

/**
 * Document class for operators.
 */
@MBPEntity(createValidator = OperatorCreateValidator.class)
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

    @DBRef
    private DataModel dataModel;

    public Operator() {
        this.routines = new ArrayList<>();
        this.parameters = new ArrayList<>();
    }

    public Operator setRoutines(List<Code> routines) {
        this.routines = routines;
        return this;
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    public Operator setDataModel(DataModel dataModel) {
        this.dataModel = dataModel;
        return this;
    }

    public List<Code> getRoutines() {
        return routines;
    }

    public Operator addRoutine(Code routine) {
        this.routines.add(routine);
        return this;
    }

    public boolean hasRoutines() {
        return !this.routines.isEmpty();
    }

    public String getId() {
        return id;
    }

    public Operator setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Operator setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Operator setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getUnit() {
        return unit;
    }

    public Operator setUnit(String unit) {
        this.unit = unit;
        return this;
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

    public Operator setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }
}