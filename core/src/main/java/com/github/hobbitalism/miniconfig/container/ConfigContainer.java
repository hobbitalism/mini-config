package com.github.hobbitalism.miniconfig.container;

import com.github.hobbitalism.miniconfig.ConfigSection;
import com.github.hobbitalism.miniconfig.annotation.Comment;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigContainer {

    private final ConverterRegistry registry;

    private final Map<Class<?>, List<Field>> fieldCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Map<Class<?>, List<Method>>> lifecycleCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, TypeConverter<?>> enumConverterCache = new ConcurrentHashMap<>();
    @SuppressWarnings("rawtypes")
    private final Map<Class, TypeConverter<?>> customConverterCache = new ConcurrentHashMap<>();

    public ConfigContainer(ConverterRegistry registry) {
        this.registry = registry;
    }

    public ConfigContainer() {
        this(ConverterRegistry.defaults());
    }

    public void load(Object target, ConfigSection section) {
        for (Field field : getCachedFields(target.getClass())) {
            bindFromSection(target, field, section);
        }
        invokeLifecycle(target, PostLoad.class);
    }

    public void save(Object target, ConfigSection section) {
        invokeLifecycle(target, PreSave.class);
        for (Field field : getCachedFields(target.getClass())) {
            writeToSection(target, field, section);
        }
    }

    private List<Field> getCachedFields(Class<?> clazz) {
        return fieldCache.computeIfAbsent(clazz, this::collectFields);
    }

    private List<Field> collectFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                if (f.isAnnotationPresent(Ignore.class))  continue;
                f.setAccessible(true);
                fields.add(f);
            }
        }
        return fields;
    }

    @SuppressWarnings({"rawtypes"})
    private void bindFromSection(Object target, Field field, ConfigSection section) {
        String path = resolvePath(field);

        Object raw;
        if (section.contains(path)) {
            raw = section.get(path).orElse(null);
        } else if (field.isAnnotationPresent(Default.class)) {
            raw = field.getAnnotation(Default.class).value();
        } else {
            return;
        }

        if (raw == null) return;

        TypeConverter converter = resolveConverter(field);
        Object converted;
        try {
            converted = converter.deserialize(raw);
        } catch (ConversionException e) {
            throw new ConfigBindingException(
                    "Failed to deserialize field '" + field.getName() + "' at path '" + path + "'", e);
        }

        try {
            field.set(target, converted);
        } catch (IllegalAccessException e) {
            throw new ConfigBindingException(
                    "Cannot set field '" + field.getName() + "'", e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void writeToSection(Object target, Field field, ConfigSection section) {
        Object value;
        try {
            value = field.get(target);
        } catch (IllegalAccessException e) {
            throw new ConfigBindingException("Cannot read field '" + field.getName() + "'", e);
        }

        if (value == null) return;

        String path = resolvePath(field);

        TypeConverter converter = resolveConverter(field);
        Object serialized;
        try {
            serialized = converter.serialize(value);
        } catch (ConversionException e) {
            throw new ConfigBindingException(
                    "Failed to serialize field '" + field.getName() + "'", e);
        }

        section.set(path, serialized);

        Comment comment = field.getAnnotation(Comment.class);
        if (comment != null && comment.value().length > 0) {
            section.setComment(path, String.join("\n", comment.value()));
        }
    }

    private static String resolvePath(Field field) {
        Path pathAnnotation = field.getAnnotation(Path.class);
        return pathAnnotation != null ? pathAnnotation.value() : field.getName();
    }

    @SuppressWarnings("rawtypes")
    private TypeConverter resolveConverter(Field field) {
        Converter converterAnnotation = field.getAnnotation(Converter.class);
        if (converterAnnotation != null) {
            Class<? extends TypeConverter<?>> converterClass = converterAnnotation.value();
            return customConverterCache.computeIfAbsent(converterClass, ConverterRegistry::instantiate);
        }
        Class<?> type = field.getType();
        TypeConverter converter = registry.get(type);
        if (converter != null) return converter;

        if (type.isEnum()) {
            return enumConverterCache.computeIfAbsent(type, ConfigContainer::enumConverter);
        }

        throw new ConfigBindingException(
                "No converter found for field '" + field.getName()
                + "' of type '" + type.getName() + "'. "
                + "Annotate with @Converter or register the type in the ConverterRegistry.");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static TypeConverter<?> enumConverter(Class<?> enumType) {
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

    private void invokeLifecycle(Object target, Class<? extends java.lang.annotation.Annotation> annotation) {
        List<Method> methods = lifecycleCache
                .computeIfAbsent(target.getClass(), k -> new ConcurrentHashMap<>())
                .computeIfAbsent(annotation, k -> {
                    List<Method> result = new ArrayList<>();
                    for (Method method : target.getClass().getDeclaredMethods()) {
                        if (!method.isAnnotationPresent(annotation)) continue;
                        if (method.getParameterCount() != 0) continue;
                        method.setAccessible(true);
                        result.add(method);
                    }
                    return result;
                });

        for (Method method : methods) {
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
