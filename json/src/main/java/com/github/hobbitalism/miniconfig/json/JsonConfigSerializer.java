package com.github.hobbitalism.miniconfig.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.hobbitalism.miniconfig.ConfigSerializer;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public class JsonConfigSerializer implements ConfigSerializer<JsonConfigSection> {

    private static final String INDENT = "  ";
    private static final String[] INDENTS = new String[16];

    static {
        for (int i = 0; i < INDENTS.length; i++) {
            INDENTS[i] = INDENT.repeat(i);
        }
    }

    private final ObjectMapper mapper;

    public JsonConfigSerializer() {
        this(JsonConfigLoader.SHARED_MAPPER.copy().enable(SerializationFeature.INDENT_OUTPUT));
    }

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
                    writeComment(gen, comment, depth + 1);
                }
                gen.writeRaw(indent(depth + 1) + "\"" + entry.getKey() + "\" : ");
                writeValue(gen, entry.getValue(), comments, path, depth + 1);
            }
            gen.writeRaw("\n" + indent(depth) + "}");
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

    private void writeComment(JsonGenerator gen, String comment, int depth) throws IOException {
        String base = indent(depth) + "// ";
        int start = 0;
        int end;
        while ((end = comment.indexOf('\n', start)) != -1) {
            gen.writeRaw(base);
            gen.writeRaw(comment, start, end - start);
            gen.writeRaw("\n");
            start = end + 1;
        }
        gen.writeRaw(base);
        gen.writeRaw(comment, start, comment.length() - start);
        gen.writeRaw("\n");
    }

    private static String indent(int depth) {
        return depth < INDENTS.length ? INDENTS[depth] : INDENT.repeat(depth);
    }
}
