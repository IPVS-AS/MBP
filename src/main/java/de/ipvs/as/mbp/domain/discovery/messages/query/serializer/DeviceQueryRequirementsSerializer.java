package de.ipvs.as.mbp.domain.discovery.messages.query.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.ipvs.as.mbp.domain.discovery.device.requirements.DeviceRequirement;
import de.ipvs.as.mbp.domain.discovery.messages.query.CandidateDevicesRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * Serializer for {@link DeviceRequirement}s of {@link CandidateDevicesRequest}s.
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
        //Create result JSON array
        JSONArray resultArray = new JSONArray();

        //Stream through all requirements, serialize and add them to the array
        requirementsList.stream().map(r -> {
            try {
                //Serialize requirement and add the type name
                return r.toQueryRequirement(new JSONObject()).put("type", r.getTypeName());
            } catch (JSONException e) {
                return new JSONObject(); //Use empty JSON object instead
            }
        }).forEach(resultArray::put);

        //Write result array to the provided generator
        gen.writeRawValue(resultArray.toString());
    }
}
