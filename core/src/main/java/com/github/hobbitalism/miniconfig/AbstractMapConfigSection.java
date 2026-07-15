package com.github.hobbitalism.miniconfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Base {@link ConfigSection} implementation backed by a {@link LinkedHashMap}.
 *
 * <p>Both the YAML and JSON adapters extend this class so that all
 * type-coercion and dot-path traversal logic lives in one place.
 * The map stores nested sections as further {@link Map} instances,
 * mirroring what parsers like SnakeYAML and Jackson naturally produce.
 *
 * <p>Dot-notation keys (e.g. {@code "database.host"}) are resolved
 * recursively: each segment navigates one level deeper into the map tree.
 */
public abstract class AbstractMapConfigSection implements ConfigSection {

    /** The in-memory data store for this section level. */
    protected final Map<String, Object> data;

    /**
     * Constructs a section wrapping the given map.
     * The map is used directly (not copied), so callers control mutability.
     *
     * @param data the backing map; must not be {@code null}
     */
    protected AbstractMapConfigSection(Map<String, Object> data) {
        this.data = data;
    }

    // -------------------------------------------------------------------------
    // Key traversal helpers
    // -------------------------------------------------------------------------

    /**
     * Splits a dot-separated path into its segments.
     */
    private static String[] split(String key) {
        return key.split("\\.", -1);
    }

    /**
     * Navigates the map tree along {@code segments[0..depth-1]}, returning
     * the map at that level, or {@code null} if any intermediate segment is
     * missing or not a map.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> navigateTo(String[] segments, int depth) {
        Map<String, Object> current = data;
        for (int i = 0; i < depth; i++) {
            Object next = current.get(segments[i]);
            if (!(next instanceof Map)) return null;
            current = (Map<String, Object>) next;
        }
        return current;
    }

    /**
     * Resolves the raw value at the given dot-separated key.
     */
    private Object rawGet(String key) {
        String[] segments = split(key);
        Map<String, Object> map = navigateTo(segments, segments.length - 1);
        if (map == null) return null;
        return map.get(segments[segments.length - 1]);
    }

    // -------------------------------------------------------------------------
    // ConfigSection — keys
    // -------------------------------------------------------------------------

    @Override
    public Set<String> getKeys(boolean deep) {
        if (!deep) {
            return Collections.unmodifiableSet(data.keySet());
        }
        Set<String> result = new java.util.LinkedHashSet<>();
        collectKeys(data, "", result);
        return Collections.unmodifiableSet(result);
    }

    @SuppressWarnings("unchecked")
    private void collectKeys(Map<String, Object> map, String prefix, Set<String> result) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String fullKey = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            result.add(fullKey);
            if (entry.getValue() instanceof Map) {
                collectKeys((Map<String, Object>) entry.getValue(), fullKey, result);
            }
        }
    }

    // -------------------------------------------------------------------------
    // ConfigSection — reads
    // -------------------------------------------------------------------------

    @Override
    public boolean contains(String key) {
        return rawGet(key) != null;
    }

    @Override
    public Optional<Object> get(String key) {
        return Optional.ofNullable(rawGet(key));
    }

    @Override
    public String getString(String key, String defaultValue) {
        Object val = rawGet(key);
        return val != null ? val.toString() : defaultValue;
    }

    @Override
    public int getInt(String key, int defaultValue) {
        Object val = rawGet(key);
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof String) {
            try { return Integer.parseInt((String) val); } catch (NumberFormatException ignored) { }
        }
        return defaultValue;
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        Object val = rawGet(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val instanceof String) {
            try { return Double.parseDouble((String) val); } catch (NumberFormatException ignored) { }
        }
        return defaultValue;
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        Object val = rawGet(key);
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof String) return Boolean.parseBoolean((String) val);
        return defaultValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getStringList(String key) {
        Object val = rawGet(key);
        if (val instanceof List) {
            List<?> raw = (List<?>) val;
            List<String> result = new ArrayList<>(raw.size());
            for (Object item : raw) result.add(item != null ? item.toString() : null);
            return result;
        }
        return Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<ConfigSection> getSection(String key) {
        Object val = rawGet(key);
        if (!(val instanceof Map)) return Optional.empty();
        return Optional.of(createChild((Map<String, Object>) val));
    }

    // -------------------------------------------------------------------------
    // ConfigSection — write
    // -------------------------------------------------------------------------

    @Override
    @SuppressWarnings("unchecked")
    public void set(String key, Object value) {
        String[] segments = split(key);
        Map<String, Object> current = data;
        for (int i = 0; i < segments.length - 1; i++) {
            Object next = current.computeIfAbsent(segments[i], k -> new LinkedHashMap<>());
            if (!(next instanceof Map)) {
                // Overwrite scalar with a map to allow nesting
                next = new LinkedHashMap<>();
                current.put(segments[i], next);
            }
            current = (Map<String, Object>) next;
        }
        if (value == null) {
            current.remove(segments[segments.length - 1]);
        } else {
            current.put(segments[segments.length - 1], value);
        }
    }

    // -------------------------------------------------------------------------
    // Hook for subclasses to produce a child section of the same concrete type
    // -------------------------------------------------------------------------

    /**
     * Factory method called when a nested map is exposed as a {@link ConfigSection}.
     * Subclasses return their own concrete type wrapping {@code childData}.
     *
     * @param childData the nested map
     * @return a {@link ConfigSection} backed by {@code childData}
     */
    protected abstract AbstractMapConfigSection createChild(Map<String, Object> childData);

    // -------------------------------------------------------------------------
    // Package-level accessor for serializers
    // -------------------------------------------------------------------------

    /**
     * Returns the raw backing map (mutable, for use by loaders/serializers).
     *
     * @return the backing map
     */
    public Map<String, Object> getRawData() {
        return data;
    }
}
