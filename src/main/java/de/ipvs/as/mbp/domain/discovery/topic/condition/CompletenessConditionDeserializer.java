package de.ipvs.as.mbp.domain.discovery.topic.condition;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.reflections.Reflections;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Deserializer for completeness conditions.
 */
public class CompletenessConditionDeserializer extends StdDeserializer<CompletenessCondition> {

    private static final String CONDITIONS_PACKAGE = "de.ipvs.as.mbp.domain.discovery.topic.condition";
    private final static Map<String, Class<? extends CompletenessCondition>> CONDITION_TYPES = new HashMap<>();

    static {
        //Get all available requirement classes
        Reflections reflections = new Reflections(CONDITIONS_PACKAGE);
        Set<Class<? extends CompletenessCondition>> requirementClasses = reflections.getSubTypesOf(CompletenessCondition.class);

        //Iterate over all requirement classes
        for (Class<? extends CompletenessCondition> condClass : requirementClasses) {
            try {
                //Create new instance of class
                CompletenessCondition condition = condClass.getDeclaredConstructor().newInstance();

                //Get and remember the name of this requirement type
                CONDITION_TYPES.put(condition.getTypeName().toLowerCase(), condClass);
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * Creates the completeness condition deserializer without any parameters. Required for bean instantiation.
     */
    public CompletenessConditionDeserializer() {
        this(null);
    }

    protected CompletenessConditionDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public CompletenessCondition deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        //Retrieve root node
        JsonNode rootNode = jsonParser.getCodec().readTree(jsonParser);

        //Check if type field is present
        if ((!rootNode.has("type")) || (!rootNode.get("type").isTextual())) {
            return null;
        }

        //Get condition type
        String type = rootNode.get("type").asText("").toLowerCase();

        //Check if a condition with this type exists
        if (!CONDITION_TYPES.containsKey(type)) {
            return null;
        }

        //Deserialize condition for its corresponding class
        return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).treeToValue(rootNode, CONDITION_TYPES.get(type));
    }
}
