package de.ipvs.as.mbp.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class InstantToStringSerializer extends StdSerializer<Instant> {

    public InstantToStringSerializer() {
        super(Instant.class);
    }

    @Override
    public void serialize(Instant instant, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(Locale.GERMAN)
                .withZone(ZoneId.systemDefault());

        jsonGenerator.writeObject(formatter.format(instant).replace(",", ""));
    }
}