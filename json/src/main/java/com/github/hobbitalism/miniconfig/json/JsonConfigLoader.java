package com.github.hobbitalism.miniconfig.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hobbitalism.miniconfig.ConfigLoader;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link ConfigLoader} that parses JSON documents into a {@link JsonConfigSection}
 * using Jackson's {@link ObjectMapper}.
 *
 * <p>A single shared (thread-safe) {@link ObjectMapper} instance is used.
 * Jackson's {@code ObjectMapper} is thread-safe once configured.
 *
 * <pre>{@code
 * JsonConfigLoader loader = new JsonConfigLoader();
 * JsonConfigSection section = loader.load(new FileReader("config.json"));
 * }</pre>
 */
public class JsonConfigLoader implements ConfigLoader<JsonConfigSection> {

    private static final TypeReference<LinkedHashMap<String, Object>> MAP_TYPE =
            new TypeReference<>() {
            };

    private final ObjectMapper mapper;

    /**
     * Constructs a loader with a default {@link ObjectMapper}.
     */
    public JsonConfigLoader() {
        this(new ObjectMapper());
    }

    /**
     * Constructs a loader with a custom {@link ObjectMapper}.
     * Use this to apply modules (e.g. {@code JavaTimeModule}) or features.
     *
     * @param mapper the Jackson object mapper to use
     */
    public JsonConfigLoader(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public JsonConfigSection load(Reader reader) throws IOException {
        Map<String, Object> map = mapper.readValue(reader, MAP_TYPE);
        return new JsonConfigSection(map == null ? new LinkedHashMap<>() : map);
    }
}
