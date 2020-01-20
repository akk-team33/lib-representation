package de.team33.libs.representation.v0;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Normalizer {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final Function<Class, BiFunction<Normalizer, Object, Object>> DEFAULT_PRODUCER =
            subjectClass -> (normalizer, subject) -> normalizer.normalFieldMap(subjectClass, subject);
    private static final String NO_ACCESS = "cannot access field <%s> for subject <%s>";
    private static final Predicate<Field> FIELD_FILTER = field -> {
        final int modifiers = field.getModifiers();
        return !(Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers));
    };
    private static final Predicate<Class<?>> IS_VALUE_CLASS = subjectClass -> {
        try {
            final boolean defaultEquals = isDefault(subjectClass, "equals", Object.class);
            final boolean defaultHashCode = isDefault(subjectClass, "hashCode");
            final boolean defaultToString = isDefault(subjectClass, "toString");
            return !(defaultEquals || defaultHashCode || defaultToString);
        } catch (final NoSuchMethodException e) {
            return false;
        }
    };

    private static boolean isDefault(final Class<?> subjectClass,
                                     final String methodName,
                                     final Class<?> ... parameters) throws NoSuchMethodException {
        return subjectClass.getMethod(methodName, parameters)
                           .getDeclaringClass()
                           .equals(Object.class);
    }

    @SuppressWarnings("rawtypes")
    private final Map<Class, BiFunction> methods;
    @SuppressWarnings("rawtypes")
    private final List<Entry> producers;

    private Normalizer(final Builder builder) {
        methods = new ConcurrentHashMap<>(builder.methods);
        producers = new ArrayList<>(builder.producers);
    }

    public static Builder builder() {
        return new Builder().addProducer(Class::isArray, subjectClass -> Normalizer::normalArray)
                            .addProducer(IS_VALUE_CLASS, subjectClass -> (normalizer, subject) -> subject)
                            .addMethod(Void.class, (normalizer, subject) -> subject);
    }

    public final Object normal(final Object subject) {
        final Class<?> subjectClass = (null == subject) ? Void.class : subject.getClass();
        return getMethod(subjectClass).apply(this, subject);
    }

    public final <T> Map<String, Object> normalFieldMap(final Class<T> subjectClass, final T subject) {
        final Map<String, Field> fieldMap = Stream.of(subjectClass.getDeclaredFields())
                                                  .filter(FIELD_FILTER)
                                                  .peek(field -> field.setAccessible(true))
                                                  .collect(Collectors.toMap(Field::getName, field -> field));
        return fieldMap.entrySet().stream()
                       .collect(TreeMap::new,
                               (map, entry) -> map.put(entry.getKey(), normal(getValue(entry.getValue(), subject))),
                               Map::putAll);
    }

    public final List<Object> normalArray(final Object array) {
        final int length = Array.getLength(array);
        final List<Object> result = new ArrayList<>(length);
        for (int i = 0; i < length; ++i) {
            result.add(normal(Array.get(array, i)));
        }
        return result;
    }

    private BiFunction<Normalizer, Object, Object> getMethod(final Class<?> subjectClass) {
        //noinspection unchecked
        return Optional.ofNullable(methods.get(subjectClass))
                       .orElseGet(() -> {
                           //noinspection rawtypes
                           final BiFunction result = getProducer(subjectClass).apply(subjectClass);
                           methods.put(subjectClass, result);
                           return result;
                       });
    }

    @SuppressWarnings("rawtypes")
    private Function<Class, BiFunction> getProducer(final Class subjectClass) {
        //noinspection unchecked
        return producers.stream()
                        .filter(entry -> entry.filter.test(subjectClass))
                        .findAny()
                        .map(entry -> entry.producer)
                        .orElse(DEFAULT_PRODUCER);
    }

    private static Object getValue(final Field field, final Object subject) {
        try {
            return field.get(subject);
        } catch (final IllegalAccessException caught) {
            throw new IllegalStateException(String.format(NO_ACCESS, field, subject), caught);
        }
    }

    private static final class Entry<T> {

        private final Predicate<Class<?>> filter;
        private final Function<Class<T>, BiFunction<Normalizer, T, Object>> producer;

        private Entry(final Predicate<Class<?>> filter,
                      final Function<Class<T>, BiFunction<Normalizer, T, Object>> producer) {
            this.filter = filter;
            this.producer = producer;
        }
    }

    public static final class Builder {

        @SuppressWarnings("rawtypes")
        private final Map<Class, BiFunction> methods = new HashMap<>(0);
        @SuppressWarnings("rawtypes")
        private final List<Entry> producers = new LinkedList<>();

        private Builder() {
        }

        public final Normalizer build() {
            return new Normalizer(this);
        }

        public final <T> Builder addMethod(final Class<T> subjectClass,
                                           final BiFunction<Normalizer, T, Object> method) {
            methods.put(subjectClass, method);
            return this;
        }

        public final <T> Builder addProducer(final Predicate<Class<?>> filter,
                                             final Function<Class<T>, BiFunction<Normalizer, T, Object>> method) {
            producers.add(new Entry<T>(filter, method));
            return this;
        }
    }
}
