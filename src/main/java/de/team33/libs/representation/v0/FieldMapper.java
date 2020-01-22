package de.team33.libs.representation.v0;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FieldMapper implements Function<Class<?>, Map<String, Field>> {

    private static final Predicate<Field> SIGNIFICANT = field -> {
        final int modifiers = field.getModifiers();
        return !(Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers));
    };

    @Override
    public Map<String, Field> apply(final Class<?> aClass) {
        return fieldStream("", aClass).collect(
                TreeMap::new,
                (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                Map::putAll);
    }

    private static Stream<Map.Entry<String, Field>> fieldStream(final Class<?> aClass) {
        return fieldStream("", aClass);
    }

    private static Stream<Map.Entry<String, Field>> fieldStream(final String prefix, final Class<?> aClass) {
        if (null == aClass) {
            return Stream.empty();
        } else {
            return Stream.concat(
                    fieldStream("." + prefix, aClass.getSuperclass()),
                    Stream.of(aClass.getDeclaredFields())
                          .filter(SIGNIFICANT)
                          .peek(field -> field.setAccessible(true))
                          .map(field -> new SimpleImmutableEntry<>(prefix + field.getName(), field)));
        }
    }
}
