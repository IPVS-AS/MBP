package de.ipvs.as.mbp.service.cep.trigger;

import com.jayway.jsonpath.JsonPath;
import de.ipvs.as.mbp.domain.data_model.IoTDataTypes;

/**
 * Stores parse instructions for one {@link de.ipvs.as.mbp.domain.valueLog.ValueLog} field.
 */
public class CEPValueLogParseInstruction {

    private String fieldName;

    private JsonPath fieldPath;

    private IoTDataTypes type;

    public CEPValueLogParseInstruction(String fieldName, JsonPath fieldPath, IoTDataTypes type) {
        // TODO
        this.fieldName = fieldName.replaceAll("`", "");
        this.fieldPath = fieldPath;
        this.type = type;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public JsonPath getFieldPath() {
        return fieldPath;
    }

    public void setFieldPath(JsonPath fieldPath) {
        this.fieldPath = fieldPath;
    }

    public IoTDataTypes getType() {
        return type;
    }

    public void setType(IoTDataTypes type) {
        this.type = type;
    }
}
