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
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Deserializer for arrays of {@link DeviceRequirement}s, provided as JSON.
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
            //Skip abstract classes and interfaces
            if (Modifier.isAbstract(reqClass.getModifiers()) || reqClass.isInterface()) continue;

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

    /**
     * Creates a new requirements deserializer for a given class.
     *
     * @param vc The class to use
     */
    protected RequirementsDeserializer(Class<?> vc) {
        super(vc);
    }

    /**
     * Method that can be called to ask implementation to deserialize
     * JSON content into the value type this serializer handles.
     * Returned instance is to be constructed by method itself.
     * <p>
     * Pre-condition for this method is that the parser points to the
     * first event that is part of value to deserializer (and which
     * is never JSON 'null' literal, more on this below): for simple
     * types it may be the only value; and for structured types the
     * Object start marker or a FIELD_NAME.
     * </p>
     * <p>
     * The two possible input conditions for structured types result
     * from polymorphism via fields. In the ordinary case, Jackson
     * calls this method when it has encountered an OBJECT_START,
     * and the method implementation must advance to the next token to
     * see the first field name. If the application configures
     * polymorphism via a field, then the object looks like the following.
     * <pre>
     *      {
     *          "@class": "class name",
     *          ...
     *      }
     *  </pre>
     * Jackson consumes the two tokens (the <tt>@class</tt> field name
     * and its value) in order to learn the class and select the deserializer.
     * Thus, the stream is pointing to the FIELD_NAME for the first field
     * after the @class. Thus, if you want your method to work correctly
     * both with and without polymorphism, you must begin your method with:
     * <pre>
     *       if (p.currentToken() == JsonToken.START_OBJECT) {
     *         p.nextToken();
     *       }
     *  </pre>
     * This results in the stream pointing to the field name, so that
     * the two conditions align.
     * <p>
     * Post-condition is that the parser will point to the last
     * event that is part of deserialized value (or in case deserialization
     * fails, event that was not recognized or usable, which may be
     * the same event as the one it pointed to upon call).
     * <p>
     * Note that this method is never called for JSON null literal,
     * and thus deserializers need (and should) not check for it.
     *
     * @param jsonParser             Parsed used for reading JSON content
     * @param deserializationContext Context that can be used to access information about this deserialization activity.
     * @return Deserialized value
     */
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
