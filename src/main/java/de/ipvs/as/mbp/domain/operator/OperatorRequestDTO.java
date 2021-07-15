package de.ipvs.as.mbp.domain.operator;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.domain.data_model.DataModel;
import de.ipvs.as.mbp.domain.operator.parameters.Parameter;
import de.ipvs.as.mbp.domain.user_entity.UserEntityRequestDTO;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.persistence.GeneratedValue;
import java.util.List;

public class OperatorRequestDTO extends UserEntityRequestDTO {

    private String name;

    private String description;

    private String unit;

    private List<Code> routines;

    private List<Parameter> parameters;

    private String dataModelId;

    // - - -

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUnit() {
        return unit;
    }

    public List<Code> getRoutines() {
        return routines;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public String getDataModelId() {
        return dataModelId;
    }
}
