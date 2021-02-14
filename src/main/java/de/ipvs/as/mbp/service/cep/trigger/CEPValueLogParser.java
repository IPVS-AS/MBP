package de.ipvs.as.mbp.service.cep.trigger;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.Document;
import org.springframework.stereotype.Service;
import de.ipvs.as.mbp.domain.valueLog.ValueLog;
import de.ipvs.as.mbp.service.cep.engine.core.events.CEPPrimitiveDataTypes;
import de.ipvs.as.mbp.domain.data_model.IoTDataTypes;
import com.jayway.jsonpath.JsonPath;
import de.ipvs.as.mbp.service.cep.engine.core.events.CEPEventType;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>Service which provides methods to parse a {@link ValueLog} to a Map with the
 * value field name as key and the valueLog value as value (type must match
 * a {@link CEPPrimitiveDataTypes}).</p>
 *
 * <p>To do this, the service stores parsing instructions for each pre-registered
 * {@link CEPEventType}. The instructions are containing a pre-compiled
 * {@link JsonPath} and a {@link IoTDataTypes} for each expected ValueLog value field.
 * These instructions get added by the {@link CEPTriggerService}.</p>
 */
@Service
public class CEPValueLogParser {

    /**
     * All known parse instructions.
     */
    private HashMap<String, Set<CEPValueLogParseInstruction>> cachedParseInstructions;

    public CEPValueLogParser() {
        cachedParseInstructions = new HashMap<>();
    }

    /**
     * Parses a {@link ValueLog} to a key-value map (value field name according to event type --> value).
     * The keys correspond to the available fields of a given {@link CEPEventType} which name is passed
     * to this method as parameter.
     *
     * @param valueLog      The ValueLog to parse.
     * @param eventTypeName The name of the CEPEventType that belongs to the ValueLog (via component reference).
     * @return A Map with the field name as key and the field value as value.
     * @throws IllegalArgumentException If for the passed eventTypeName no parse instructions are known.
     */
    public Map<String, Object> parseValueLog(ValueLog valueLog, String eventTypeName) throws IllegalArgumentException {

        //Sanity check
        if (valueLog == null) {
            throw new IllegalArgumentException("Value log must not be null.");
        }

        Map<String, Object> returnMap = new HashMap<>();

        // Check if the eventType is known by the parser. If not throw an exception.
        if (!(this.cachedParseInstructions.containsKey(eventTypeName))) {
            throw new IllegalArgumentException("The CEPValueLogParser does not have instructions for the CEPEventType" +
                    "named " + eventTypeName + ".");
        }

        // Get the parse instructions
        Set<CEPValueLogParseInstruction> parseInstructions = this.cachedParseInstructions.get(eventTypeName);

        // Get the value document
        Document values = valueLog.getValue();
        String jsonValue = values.toJson();

        // Apply the parse instructions
        for (CEPValueLogParseInstruction instruction : parseInstructions) {
            if (IoTDataTypes.hasCepPrimitiveDataType(instruction.getType())) {
                returnMap.put(instruction.getFieldName(), instruction.getFieldPath().read(jsonValue));
            } else {
                switch (instruction.getType()) {
                    case DATE:
                        // TODO DOES THIS WORK? HOW ARE DATES SENDED VIA JSON?
                        Date parsedDate = new Date();
                        parsedDate.setTime(instruction.getFieldPath().read(jsonValue));
                        returnMap.put(instruction.getFieldName(), parsedDate.getTime());
                        break;
                    case BINARY:
                        String binaryString = instruction.getFieldPath().read(jsonValue);
                        returnMap.put(instruction.getFieldName(), binaryString);
                        break;
                    default:
                        continue;
                }
            }
        }

        return returnMap;
    }

    /**
     * Adds a set of {@link CEPValueLogParseInstruction}s to the parse instruction
     * cache.
     *
     * @param eventTypeName     The name of the {@link CEPEventType} to which this instruction belongs.
     * @param parseInstructions A set of single {@link CEPValueLogParseInstruction}s.
     */
    public void addInstructionsForEventType(String eventTypeName, Set<CEPValueLogParseInstruction> parseInstructions) {
        System.out.println("Added " + eventTypeName);
        this.cachedParseInstructions.put(eventTypeName, parseInstructions);
    }

    /**
     * Removes the parse instructions for a given eventTypeName.
     *
     * @param eventTypeName The name of the {@link CEPEventType} to remove from the instruction cache.
     */
    public void removeParseInstructionsForEventType(String eventTypeName) {
        this.cachedParseInstructions.remove(eventTypeName);
    }


}
