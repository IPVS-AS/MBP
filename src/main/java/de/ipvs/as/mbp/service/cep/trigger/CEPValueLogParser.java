package de.ipvs.as.mbp.service.cep.trigger;

import com.jayway.jsonpath.ReadContext;
import de.ipvs.as.mbp.service.receiver.ValueLogReceiveVerifier;
import org.bson.Document;
import org.springframework.stereotype.Service;
import de.ipvs.as.mbp.domain.valueLog.ValueLog;
import de.ipvs.as.mbp.service.cep.engine.core.events.CEPPrimitiveDataTypes;
import de.ipvs.as.mbp.domain.data_model.DataModelDataType;
import com.jayway.jsonpath.JsonPath;
import de.ipvs.as.mbp.service.cep.engine.core.events.CEPEventType;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

/**
 * <p>Service which provides methods to parse a {@link ValueLog} to a Map with the
 * value field name as key and the valueLog value as value (type must match
 * a {@link CEPPrimitiveDataTypes}).</p>
 *
 * <p>To do this, the service stores parsing instructions for each pre-registered
 * {@link CEPEventType}. The instructions are containing a pre-compiled
 * {@link JsonPath} and a {@link DataModelDataType} for each expected ValueLog value field.
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

        // Add standard parse instructions for monitoring values
        CEPValueLogParseInstruction monitoringParseInstructions = new CEPValueLogParseInstruction(
                "value",
                JsonPath.compile("$['value']"),
                DataModelDataType.DOUBLE
        );
        HashSet<CEPValueLogParseInstruction> monitoringInstructionSet = new HashSet();
        monitoringInstructionSet.add(monitoringParseInstructions);
        cachedParseInstructions.put("MONITORING", monitoringInstructionSet);
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

        // Extra handling for monitoring components
        if (valueLog.getComponent().equals("MONITORING")) {
            eventTypeName = "MONITORING";
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

        // Parse the json path for the JsonPath library once
        ReadContext ctx = JsonPath.parse(jsonValue);

        // Apply the parse instructions
        for (CEPValueLogParseInstruction instruction : parseInstructions) {
            if (instruction.getType() == DataModelDataType.DOUBLE || instruction.getType() == DataModelDataType.INT ||
                    instruction.getType() == DataModelDataType.STRING
            || instruction.getType() == DataModelDataType.BOOLEAN) {
                returnMap.put(instruction.getFieldName(), ctx.read(instruction.getFieldPath()));
            } else {
                LinkedHashMap<String, Object> extractedObj = ctx.read(instruction.getFieldPath());
                switch (instruction.getType()) {
                    case LONG:
                        returnMap.put(instruction.getFieldName(),Long.parseLong((String) extractedObj.get("$numberLong")));
                        break;
                    case DECIMAL128:
                        returnMap.put(instruction.getFieldName(), new BigDecimal((String) extractedObj.get("$numberDecimal")));
                        break;
                    case DATE:
                        Date parsedDate = null;
                        if (extractedObj.get("$date") instanceof Long) {
                            parsedDate = ValueLogReceiveVerifier.parseDateLong((Long) extractedObj.get("$date"));
                        } else if (extractedObj.get(extractedObj.keySet().toArray()[0]) instanceof String) {
                            try {
                                parsedDate = ValueLogReceiveVerifier.parseDateString((String) extractedObj.get("$date"));
                            } catch (ParseException e) {
                                throw new IllegalArgumentException("Error by converting date for CEP.");
                            }
                        }
                        returnMap.put(instruction.getFieldName(), parsedDate.getTime());
                        break;
                    case BINARY:
                        // For CEP no binary type exists --> handle it as string TODO binary exists in esper maybe add it later
                        String getBinaryString = (String) extractedObj.get("$binary");
                        returnMap.put(instruction.getFieldName(), getBinaryString);
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
