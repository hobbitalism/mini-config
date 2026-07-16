package com.github.hobbitalism.miniconfig.json;

import com.github.hobbitalism.miniconfig.AbstractMapConfigSection;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A {@link com.github.hobbitalism.miniconfig.ConfigSection} backed by a
 * {@link LinkedHashMap} parsed from JSON via Jackson.
 *
 * <p>Jackson's {@code ObjectMapper} is configured with
 * {@link com.fasterxml.jackson.databind.DeserializationFeature#USE_LONG_FOR_INTS}
 * disabled, meaning whole-number JSON values arrive as {@link Integer} or
 * {@link Long} depending on magnitude — both handled transparently by
 * {@link AbstractMapConfigSection#getInt}.
 */
public class JsonConfigSection extends AbstractMapConfigSection {

    private Map<String, String> comments;

    /**
     * Constructs a section wrapping the given map.
     *
     * @param data the backing map; must not be {@code null}
     */
    public JsonConfigSection(Map<String, Object> data) {
        super(data);
    }

    /**
     * Constructs an empty section.
     */
    public JsonConfigSection() {
        super(new LinkedHashMap<>());
    }

    /**
     * Associates a comment with a config path.
     *
     * @param path    the dot-separated config path
     * @param comment the comment text (without {@code //})
     */
    public void setComment(String path, String comment) {
        if (comments == null) {
            comments = new LinkedHashMap<>();
        }
        comments.put(path, comment);
    }

    /**
     * Returns the map of path → comment, or an empty map if none set.
     */
    public Map<String, String> getComments() {
        return comments != null ? comments : Collections.emptyMap();
    }

    @Override
    protected JsonConfigSection createChild(Map<String, Object> childData) {
        return new JsonConfigSection(childData);
    }
}
