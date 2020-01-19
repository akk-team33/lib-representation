package de.team33.libs.representation.v0;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

public class Normalizer {

    private static final String NOT_FOUND = "cannot find a method or producer for %s";
    private static final String NO_ACCESS = "cannot access field <%s> for subject <%s>";

    private final Map<Class, BiFunction> methods;
    private final List<Entry> producers;

    private static class Entry<T> {

        private final Predicate<Class<T>> filter;
        private final Function<Class<T>, BiFunction<Normalizer, T, Object>> producer;

        private Entry(final Predicate<Class<T>> filter,
                      final Function<Class<T>, BiFunction<Normalizer, T, Object>> producer) {
            this.filter = filter;
            this.producer = producer;
        }
    }

    private Normalizer(final Builder builder) {
        methods = new ConcurrentHashMap<>(builder.methods);
        producers = new ArrayList<>(builder.producers);
    }

    public static Builder builder() {
        return new Builder();
    }

    public final Object normal(final Object subject) {
        final Class<?> subjectClass = (null == subject) ? Void.class : subject.getClass();
        return getMethod(subjectClass).apply(this, subject);
    }

    private BiFunction<Normalizer, Object, Object> getMethod(final Class<?> subjectClass) {
        //noinspection unchecked
        return Optional.ofNullable(methods.get(subjectClass))
                       .orElseGet(() -> {
                           final Function<Class, BiFunction> producer = getProducer(subjectClass);
                           final BiFunction result = producer.apply(subjectClass);
                           methods.put(subjectClass, result);
                           return result;
                       });
    }

    private Function<Class, BiFunction> getProducer(final Class subjectClass) {
        //noinspection unchecked
        return producers.stream()
                        .filter(entry -> entry.filter.test(subjectClass))
                        .findAny()
                        .map(entry -> entry.producer)
                        .orElseThrow(() -> new IllegalArgumentException(format(NOT_FOUND, subjectClass)));
    }

    public <T> Map<String, Object> normalFieldMap(final Class<T> subjectClass, final T subject) {
        final Map<String, Field> fieldMap = Stream.of(subjectClass.getDeclaredFields())
                                                  .filter(field -> {
                                                      final int modifiers = field.getModifiers();
                                                      return !(Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers));
                                                  })
                                                  .peek(field -> field.setAccessible(true))
                                                  .collect(Collectors.toMap(Field::getName, field -> field));
        return fieldMap.entrySet().stream()
                       .collect(Collectors.toMap(Map.Entry::getKey,
                                                 entry -> normal(getValue(entry.getValue(), subject))));
    }

    private Object getValue(final Field field, final Object subject) {
        try {
            return field.get(subject);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(String.format(NO_ACCESS, field, subject));
        }
    }

    public static class Builder {

        private final Map<Class, BiFunction> methods = new HashMap<>(0);
        private final List<Entry> producers = new LinkedList<>();

        public final Normalizer build() {
            return new Normalizer(this);
        }

        public final <T> Builder addMethod(final Class<T> subjectClass,
                                           final BiFunction<Normalizer, T, Object> method) {
            methods.put(subjectClass, method);
            return this;
        }
    }

}
