package com.github.hobbitalism.miniconfig.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.hobbitalism.miniconfig.ConfigSerializer;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

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

    private static final String INDENT = "  ";

    private final ObjectMapper mapper;

    /**
     * Constructs a serializer with a default pretty-printing {@link ObjectMapper}.
     */
    public JsonConfigSerializer() {
        this(new ObjectMapper()
                .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
                .enable(SerializationFeature.INDENT_OUTPUT));
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
        Map<String, String> comments = section.getComments();
        try (JsonGenerator gen = mapper.getFactory().createGenerator(writer)) {
            writeValue(gen, section.getRawData(), comments, "", 0);
        }
    }

    @SuppressWarnings("unchecked")
    private void writeValue(JsonGenerator gen, Object value,
                             Map<String, String> comments, String prefix, int depth) throws IOException {
        if (value instanceof Map) {
            gen.writeRaw("{\n");
            Map<String, Object> map = (Map<String, Object>) value;
            int count = 0;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (count++ > 0) gen.writeRaw(",\n");
                String path = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                String comment = comments != null ? comments.get(path) : null;
                if (comment != null) {
                    for (String line : comment.split("\n")) {
                        gen.writeRaw(INDENT.repeat(depth + 1) + "// " + line + "\n");
                    }
                }
                gen.writeRaw(INDENT.repeat(depth + 1) + "\"" + entry.getKey() + "\" : ");
                writeValue(gen, entry.getValue(), comments, path, depth + 1);
            }
            gen.writeRaw("\n" + INDENT.repeat(depth) + "}");
        } else if (value instanceof List) {
            gen.writeRaw("[");
            List<?> list = (List<?>) value;
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) gen.writeRaw(", ");
                writeValue(gen, list.get(i), null, null, depth);
            }
            gen.writeRaw("]");
        } else {
            gen.writeObject(value);
        }
    }
}
