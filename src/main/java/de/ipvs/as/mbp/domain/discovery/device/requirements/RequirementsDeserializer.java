package de.ipvs.as.mbp.domain.discovery.device.requirements;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.util.Json;
import org.reflections.Reflections;

import java.io.IOException;
import java.util.*;

/**
 * Deserializer for lists of requirements, provided as JSON strings.
 */
public class RequirementsDeserializer extends StdDeserializer<List<DeviceRequirement>> {

    private static final String REQUIREMENTS_PACKAGE = "de.ipvs.as.mbp.domain.discovery.device.requirements";
    private final static Map<String, Class<? extends DeviceRequirement>> REQUIREMENT_TYPES = new HashMap<>();

    static {
        //Get all available requirement classes
        Reflections reflections = new Reflections(REQUIREMENTS_PACKAGE);
        Set<Class<? extends DeviceRequirement>> requirementClasses = reflections.getSubTypesOf(DeviceRequirement.class);

        //Iterate over all requirement classes
        for (Class<? extends DeviceRequirement> reqClass : requirementClasses) {
            try {
                //Create new instance of class
                DeviceRequirement requirement = reqClass.getDeclaredConstructor().newInstance();

                //Get and remember the name of this requirement type
                REQUIREMENT_TYPES.put(requirement.getTypeName().toLowerCase(), reqClass);
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * Creates the requirements deserializer without any parameters. Required for bean instantiation.
     */
    public RequirementsDeserializer() {
        this(null);
    }

    protected RequirementsDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public List<DeviceRequirement> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        //Retrieve root node
        JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);

        //Root node must be an array
        if (!rootNode.isArray()) {
            throw new EntityValidationException("Invalid format.");
        }

        //Create result list
        List<DeviceRequirement> requirementsList = new ArrayList<>();

        //Iterate over the array of requirements
        for (JsonNode node : rootNode) {
            //Check if type field is present
            if ((!node.has("type")) || (!node.get("type").isTextual())) {
                continue;
            }

            //Get requirement type
            String type = node.get("type").asText("").toLowerCase();

            //Check if a requirement with this type exists
            if (!REQUIREMENT_TYPES.containsKey(type)) {
                continue;
            }

            //Deserialize requirement for its corresponding class
            DeviceRequirement requirement = Json.MAPPER.treeToValue(node, REQUIREMENT_TYPES.get(type));

            //Check if requirement is valid and add it to list
            if (requirement != null) {
                requirementsList.add(requirement);
            }
        }

        //Return resulting requirements list
        return requirementsList;
    }
}
