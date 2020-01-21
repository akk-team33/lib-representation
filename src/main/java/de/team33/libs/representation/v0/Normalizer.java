package de.team33.libs.representation.v0;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Normalizer {

    private static final Function<Class<Object>, BiFunction<Normalizer, Object, Object>> ARRAY_PRODUCER =
            subjectClass -> Normalizer::normalArray;
    private static final Function<Class<Collection<?>>, BiFunction<Normalizer, Collection<?>, Object>> LIST_PRODUCER =
            subjectClass -> Normalizer::normalList;
    private static final String NO_ACCESS = "cannot access field <%s> for subject <%s>";
    private static final Predicate<Field> FIELD_FILTER = field -> {
        final int modifiers = field.getModifiers();
        return !(Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers));
    };
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final Function<Class, BiFunction<Normalizer, Object, Object>> DEFAULT_PRODUCER =
            subjectClass -> (normalizer, subject) -> normalizer.normalFieldMap(subjectClass, subject);
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
    @SuppressWarnings("rawtypes")
    private final Map<Class, BiFunction> methods;
    private Normalizer(final Builder builder) {
        methods = new ConcurrentHashMap<>(builder.methods);
    }

    private static boolean isDefault(final Class<?> subjectClass,
                                     final String methodName,
                                     final Class<?> ... parameters) throws NoSuchMethodException {
        return subjectClass.getMethod(methodName, parameters)
                           .getDeclaringClass()
                           .equals(Object.class);
    }

    public static Builder builder() {
        return new Builder();
    }

    private static Object getValue(final Field field, final Object subject) {
        try {
            return field.get(subject);
        } catch (final IllegalAccessException caught) {
            throw new IllegalStateException(String.format(NO_ACCESS, field, subject), caught);
        }
    }

    public final Object normal(final Object subject) {
        final Class<?> subjectClass = (null == subject) ? Void.class : subject.getClass();
        return getMethod(subjectClass).apply(this, subject);
    }

    private BiFunction<Normalizer, Object, Object> getMethod(final Class<?> subjectClass) {
        //noinspection unchecked
        return Optional.ofNullable(methods.get(subjectClass))
                       .orElseGet(() -> {

                           //noinspection rawtypes
                           final BiFunction result = Category.of(subjectClass).producer.apply(subjectClass);
                           methods.put(subjectClass, result);
                           return result;
                       });
    }

    public final Map<String, Object> normalFieldMap(final Class<?> subjectClass, final Object subject) {
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

    public final List<Object> normalList(final Collection<?> subject) {
        return subject.stream()
                      .map(this::normal)
                      .collect(Collectors.toList());
    }

    public final Set<?> normalSet(final Set<?> subject) {
        return subject.stream()
                      .map(this::normal)
                      .collect(Collectors.toSet());
    }

    public final Map<?, ?> normalMap(final Map<?, ?> subject) {
        return subject.entrySet().stream().collect(
                HashMap::new, (map, entry) -> map.put(normal(entry.getKey()), normal(entry.getValue())), Map::putAll);
    }

    private enum Category {

        VOID(Void.class::equals, subjectClass -> (normalizer, subject) -> subject),
        ARRAY(Class::isArray, subjectClass -> Normalizer::normalArray),
        SET(Set.class::isAssignableFrom, subjectClass -> (normalizer, subject) -> normalizer.normalSet((Set<?>) subject)),
        LIST(Collection.class::isAssignableFrom, subjectClass -> (normalizer, subject) -> normalizer.normalList((Collection<?>) subject)),
        MAP(Map.class::isAssignableFrom, subjectClass -> (normalizer, subject) -> normalizer.normalMap((Map<?, ?>) subject)),
        SIMPLE(IS_VALUE_CLASS, subjectClass -> (normalizer, subject) -> subject),
        FIELD_MAPPED(
                subjectClass -> true,
                subjectClass -> (normalizer, subject) -> normalizer.normalFieldMap(subjectClass, subject));

        private final Predicate<Class<?>> filter;
        private final Function<Class<?>, BiFunction<Normalizer, Object, Object>> producer;

        Category(final Predicate<Class<?>> filter, final Function<Class<?>, BiFunction<Normalizer, Object, Object>> producer) {
            this.filter = filter;
            this.producer = producer;
        }

        private static Category of(final Class<?> subjectClass) {
            return Stream.of(values())
                         .filter(value -> value.filter.test(subjectClass))
                         .findFirst()
                         .orElse(FIELD_MAPPED);
        }
    }

    public static final class Builder {

        @SuppressWarnings("rawtypes")
        private final Map<Class, BiFunction> methods = new HashMap<>(0);

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
    }
}
