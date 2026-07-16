package com.github.hobbitalism.miniconfig.yaml;

import com.github.hobbitalism.miniconfig.AbstractMapConfigSection;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A {@link com.github.hobbitalism.miniconfig.ConfigSection} backed by a
 * {@link LinkedHashMap} parsed from YAML via SnakeYAML.
 *
 * <p>Insertion order is preserved (SnakeYAML produces {@link LinkedHashMap}s
 * by default when configured with {@code DumperOptions}).
 */
public class YamlConfigSection extends AbstractMapConfigSection {

    private Map<String, String> comments;

    /**
     * Constructs a section wrapping the given map.
     *
     * @param data the backing map; must not be {@code null}
     */
    public YamlConfigSection(Map<String, Object> data) {
        super(data);
    }

    /**
     * Constructs an empty section.
     */
    public YamlConfigSection() {
        super(new LinkedHashMap<>());
    }

    /**
     * Associates a comment with a config path.
     *
     * @param path    the dot-separated config path
     * @param comment the comment text (without {@code #})
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
    protected YamlConfigSection createChild(Map<String, Object> childData) {
        return new YamlConfigSection(childData);
    }
}
