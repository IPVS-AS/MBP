package de.ipvs.as.mbp.domain.discovery.messages.query.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.ipvs.as.mbp.domain.discovery.device.requirements.DeviceRequirement;
import de.ipvs.as.mbp.domain.discovery.messages.query.DeviceQueryRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * Serializer for {@link DeviceRequirement}s of {@link DeviceQueryRequest}s.
 */
public class DeviceQueryRequirementsSerializer extends JsonSerializer<List<DeviceRequirement>> {

    /**
     * Method that can be called to ask implementation to serialize
     * values of type this serializer handles.
     *
     * @param requirementsList Value to serialize; can <b>not</b> be null.
     * @param gen              Generator used to output resulting Json content
     * @param serializers      Provider that can be used to get serializers for
     */
    @Override
    public void serialize(List<DeviceRequirement> requirementsList, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        //Start array
        gen.writeStartArray();

        //Stream through all device requirements and serialize them
        requirementsList.forEach(r -> {
            try {
                //Serialize requirement and add separator
                gen.writeRaw(r.toQueryRequirement(new JSONObject()).toString());
                gen.writeRaw(",");
            } catch (JSONException | IOException ignored) {
            }
        });

        //End array
        gen.writeEndArray();
    }
}
