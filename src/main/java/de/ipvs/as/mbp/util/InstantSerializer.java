package de.ipvs.as.mbp.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Instant;

public class InstantSerializer extends StdSerializer<Instant> {

    public InstantSerializer() {
        super(Instant.class);
    }

    @Override
    public void serialize(Instant item, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        jsonGenerator.writeObject(item.toString());
    }
}