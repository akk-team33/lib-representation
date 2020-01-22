package de.team33.libs.representation.v0;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Translator {

    private static final String NO_ACCESS = "cannot access field <%s> of subject <%s>";

    private final Map<Class<?>, Map<String, Field>> fieldMaps = new ConcurrentHashMap<>(0);

    public abstract Object normal(final Object subject);

    public final List<?> normalArray(final Object array) {
        final int size = Array.getLength(array);
        final List<Object> result = new ArrayList<>(size);
        for (int index = 0; index < size; ++index) {
            result.add(normal(Array.get(array, index)));
        }
        return result;
    }

    public final <S> List<?> normalList(final S subject, final Function<? super S, ? extends Stream<?>> toStream) {
        return new ArrayList<>(normalCollection(LinkedList::new, subject, toStream));
    }

    public final <S> Set<?> normalSet(final S subject, final Function<? super S, ? extends Stream<?>> toStream) {
        return normalCollection(HashSet::new, subject, toStream);
    }

    public final <S> Map<?, ?> normalMap(final S subject,
                                         final Function<? super S, ? extends Stream<Map.Entry<?, ?>>> toStream) {
        return toStream.apply(subject)
                       .collect(HashMap::new,
                               (map, entry) -> map.put(normal(entry.getKey()), normal(entry.getValue())),
                               Map::putAll);
    }

    public final <S> Map<?, ?> normalFieldMap(final S subject,
                                              final Function<? super Class<?>, ? extends Stream<Field>> toStream,
                                              final Function<? super Field, String> toFieldName) {
        Optional.ofNullable(fieldMaps.get(subject.getClass()))
                .orElseGet(() -> newFieldMap(subject.getClass()));
        return toStream.apply(subject.getClass())
                       .collect(HashMap::new,
                               (map, field) -> map.put(
                                       normal(toFieldName.apply(field)),
                                       normal(getValue(field, subject))),
                               Map::putAll);
    }

    private static Object getValue(final Field field, final Object subject) {
        try {
            return field.get(subject);
        } catch (final IllegalAccessException e) {
            throw new IllegalArgumentException(String.format(NO_ACCESS, field, subject), e);
        }
    }

    private <S, C extends Collection<Object>> C normalCollection(final Supplier<C> newCollection, final S subject,
                                                                 final Function<? super S, ? extends Stream<?>> toStream) {
        return toStream.apply(subject)
                       .map(this::normal)
                       .collect(Collectors.toCollection(newCollection));
    }
}
