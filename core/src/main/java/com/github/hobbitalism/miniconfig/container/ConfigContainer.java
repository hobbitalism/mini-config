package com.github.hobbitalism.miniconfig.container;

import com.github.hobbitalism.miniconfig.ConfigSection;
import com.github.hobbitalism.miniconfig.annotation.Converter;
import com.github.hobbitalism.miniconfig.annotation.Default;
import com.github.hobbitalism.miniconfig.annotation.Ignore;
import com.github.hobbitalism.miniconfig.annotation.Path;
import com.github.hobbitalism.miniconfig.annotation.PostLoad;
import com.github.hobbitalism.miniconfig.annotation.PreSave;
import com.github.hobbitalism.miniconfig.convert.ConversionException;
import com.github.hobbitalism.miniconfig.convert.TypeConverter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Binds the fields of a {@code @Config}-annotated POJO to and from a
 * {@link ConfigSection}, honouring {@link Path}, {@link Default},
 * {@link Ignore}, {@link Converter}, {@link PostLoad}, and {@link PreSave}.
 *
 * <h2>Load ({@link ConfigSection} → POJO)</h2>
 * <ol>
 *   <li>Every non-static, non-ignored field is resolved to a config path
 *       (from {@link Path#value()} or the field name).</li>
 *   <li>The raw value is read from the section. If absent and
 *       {@link Default} is present, the default string is used instead.</li>
 *   <li>The value is converted to the field type via the appropriate
 *       {@link TypeConverter} and injected reflectively.</li>
 *   <li>All {@link PostLoad}-annotated methods are invoked.</li>
 * </ol>
 *
 * <h2>Save (POJO → {@link ConfigSection})</h2>
 * <ol>
 *   <li>All {@link PreSave}-annotated methods are invoked.</li>
 *   <li>Every non-static, non-ignored field is serialized via its converter
 *       and written to the section at the resolved path.</li>
 * </ol>
 *
 * <pre>{@code
 * ConverterRegistry registry = ConverterRegistry.defaults();
 * ConfigContainer container = new ConfigContainer(registry);
 *
 * YamlConfig file = new YamlConfig(Path.of("config.yml"));
 * file.load();
 *
 * PluginConfig cfg = new PluginConfig();
 * container.load(cfg, file);     // section → POJO
 * // ... mutate cfg ...
 * container.save(cfg, file);     // POJO → section
 * file.save();
 * }</pre>
 */
public class ConfigContainer {

    private final ConverterRegistry registry;

    /**
     * Constructs a container backed by the given registry.
     *
     * @param registry converter registry to use for all type coercions
     */
    public ConfigContainer(ConverterRegistry registry) {
        this.registry = registry;
    }

    /**
     * Constructs a container using {@link ConverterRegistry#defaults()}.
     */
    public ConfigContainer() {
        this(ConverterRegistry.defaults());
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Reads values from {@code section} into all eligible fields of {@code target},
     * then calls all {@link PostLoad} methods.
     *
     * @param target  the config POJO to populate
     * @param section the source section
     * @throws ConfigBindingException if any field cannot be bound
     */
    public void load(Object target, ConfigSection section) {
        for (Field field : collectFields(target.getClass())) {
            bindFromSection(target, field, section);
        }
        invokeLifecycle(target, PostLoad.class);
    }

    /**
     * Calls all {@link PreSave} methods on {@code target}, then writes all
     * eligible fields into {@code section}.
     *
     * @param target  the config POJO to read from
     * @param section the destination section
     * @throws ConfigBindingException if any field cannot be serialized
     */
    public void save(Object target, ConfigSection section) {
        invokeLifecycle(target, PreSave.class);
        for (Field field : collectFields(target.getClass())) {
            writeToSection(target, field, section);
        }
    }

    // -------------------------------------------------------------------------
    // Field collection
    // -------------------------------------------------------------------------

    /**
     * Returns all instance fields declared in {@code clazz} and its superclasses,
     * excluding static fields and fields annotated with {@link Ignore}.
     */
    private List<Field> collectFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                if (f.isAnnotationPresent(Ignore.class))  continue;
                fields.add(f);
            }
        }
        return fields;
    }

    // -------------------------------------------------------------------------
    // Load path
    // -------------------------------------------------------------------------

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void bindFromSection(Object target, Field field, ConfigSection section) {
        String path = resolvePath(field);

        // 1. Read raw value; fall back to @Default if absent
        Object raw;
        if (section.contains(path)) {
            raw = section.get(path).orElse(null);
        } else if (field.isAnnotationPresent(Default.class)) {
            raw = field.getAnnotation(Default.class).value();
        } else {
            // No value and no default — leave field at its Java initializer value
            return;
        }

        if (raw == null) return;

        // 2. Determine converter
        TypeConverter converter = resolveConverter(field);

        // 3. Coerce and inject
        Object converted;
        try {
            converted = converter.deserialize(raw);
        } catch (ConversionException e) {
            throw new ConfigBindingException(
                    "Failed to deserialize field '" + field.getName() + "' at path '" + path + "'", e);
        }

        field.setAccessible(true);
        try {
            field.set(target, converted);
        } catch (IllegalAccessException e) {
            throw new ConfigBindingException(
                    "Cannot set field '" + field.getName() + "'", e);
        }
    }

    // -------------------------------------------------------------------------
    // Save path
    // -------------------------------------------------------------------------

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void writeToSection(Object target, Field field, ConfigSection section) {
        field.setAccessible(true);
        Object value;
        try {
            value = field.get(target);
        } catch (IllegalAccessException e) {
            throw new ConfigBindingException("Cannot read field '" + field.getName() + "'", e);
        }

        if (value == null) return;

        TypeConverter converter = resolveConverter(field);
        Object serialized;
        try {
            serialized = converter.serialize(value);
        } catch (ConversionException e) {
            throw new ConfigBindingException(
                    "Failed to serialize field '" + field.getName() + "'", e);
        }

        section.set(resolvePath(field), serialized);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the config path for a field: {@link Path#value()} if present,
     * otherwise the field name.
     */
    private static String resolvePath(Field field) {
        Path pathAnnotation = field.getAnnotation(Path.class);
        return pathAnnotation != null ? pathAnnotation.value() : field.getName();
    }

    /**
     * Resolves the {@link TypeConverter} to use for a field.
     *
     * <p>Priority:
     * <ol>
     *   <li>{@link Converter @Converter} annotation on the field — instantiated reflectively.</li>
     *   <li>Registry lookup by the field's declared type.</li>
     * </ol>
     */
    @SuppressWarnings("rawtypes")
    private TypeConverter resolveConverter(Field field) {
        Converter converterAnnotation = field.getAnnotation(Converter.class);
        if (converterAnnotation != null) {
            return ConverterRegistry.instantiate(converterAnnotation.value());
        }
        Class<?> type = field.getType();
        TypeConverter converter = registry.get(type);
        if (converter != null) return converter;

        // Enum fallback: convert by name
        if (type.isEnum()) {
            return enumConverter(type);
        }

        throw new ConfigBindingException(
                "No converter found for field '" + field.getName()
                + "' of type '" + type.getName() + "'. "
                + "Annotate with @Converter or register the type in the ConverterRegistry.");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static TypeConverter enumConverter(Class<?> enumType) {
        return new TypeConverter() {
            @Override
            public Object deserialize(Object raw) {
                try {
                    return Enum.valueOf((Class<Enum>) enumType, raw.toString().trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new ConversionException(
                            "Unknown enum constant '" + raw + "' for " + enumType.getSimpleName(), e);
                }
            }
            @Override
            public Object serialize(Object value) {
                return ((Enum<?>) value).name();
            }
        };
    }

    // -------------------------------------------------------------------------
    // Lifecycle hooks
    // -------------------------------------------------------------------------

    private static void invokeLifecycle(Object target, Class<? extends java.lang.annotation.Annotation> annotation) {
        for (Method method : target.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(annotation)) continue;
            if (method.getParameterCount() != 0) continue;
            method.setAccessible(true);
            try {
                method.invoke(target);
            } catch (java.lang.reflect.InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) throw (RuntimeException) cause;
                throw new ConfigBindingException(
                        "Lifecycle method '" + method.getName() + "' threw an exception", cause);
            } catch (IllegalAccessException e) {
                throw new ConfigBindingException(
                        "Cannot invoke lifecycle method '" + method.getName() + "'", e);
            }
        }
    }
}
