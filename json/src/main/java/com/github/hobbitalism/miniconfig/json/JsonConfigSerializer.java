package com.github.hobbitalism.miniconfig.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.hobbitalism.miniconfig.ConfigSerializer;

import java.io.IOException;
import java.io.Writer;

/**
 * {@link ConfigSerializer} that writes a {@link JsonConfigSection} to pretty-printed
 * JSON using Jackson's {@link ObjectMapper}.
 *
 * <pre>{@code
 * JsonConfigSerializer serializer = new JsonConfigSerializer();
 * serializer.save(section, new FileWriter("config.json"));
 * }</pre>
 */
public class JsonConfigSerializer implements ConfigSerializer<JsonConfigSection> {

    private final ObjectMapper mapper;

    /**
     * Constructs a serializer with a default pretty-printing {@link ObjectMapper}.
     */
    public JsonConfigSerializer() {
        this(new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT));
    }

    /**
     * Constructs a serializer with a custom {@link ObjectMapper}.
     *
     * @param mapper the Jackson object mapper to use for serialization
     */
    public JsonConfigSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(JsonConfigSection section, Writer writer) throws IOException {
        mapper.writeValue(writer, section.getRawData());
    }
}
