package de.team33.test.representation.v0;

import com.google.common.collect.ImmutableMap;
import de.team33.libs.representation.v0.Normalizer;
import de.team33.test.representation.shared.Subject;
import org.junit.Test;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class NormalizerTest {

    private final Random random = new Random();
    private final Normalizer normalizer = Normalizer.builder().build();

    @Test
    public final void normal() {
        final Subject subject = new Subject().setThePrimitive(random.nextInt())
                                             .setTheString(anyString())
                                             .setTheNumber(random.nextDouble())
                                             .setTheByteArray(anyBytes())
                                             .setTheList(asList(random.nextInt(), anyString(), new Date(), new Subject(), null))
                                             .setTheSet(asList(random.nextInt(), anyBytes(), new Date(), new Subject(), null))
                                             .setTheMap(ImmutableMap.builder()
                                                                    .put(anyString(), anyBytes())
                                                                    .put(random.nextInt(), new Subject())
                                                                    .put(new Subject(), new Date())
                                                                    .build());
        final Object result = normalizer.normal(subject);
        assertEquals(expected(subject), result);
    }

    private static Map<String, Object> expected(final Subject subject) {
        final Map<String, Object> result = new TreeMap<>();
        result.put("thePrimitive", subject.getThePrimitive());
        result.put("theString", subject.getTheString());
        result.put("theNumber", subject.getTheNumber());
        result.put("theByteArray", expected(subject.getTheByteArray()));
        result.put("theObject", subject.getTheObject());
        result.put("theList", expected(subject.getTheList()));
        result.put("theSet", expected(subject.getTheSet()));
        result.put("theMap", expected(subject.getTheMap()));
        return result;
    }

    private static Set<?> expected(final Set<?> subjects) {
        return (null == subjects) ? null : subjects.stream().map(NormalizerTest::expected).collect(Collectors.toSet());
    }

    private static List<?> expected(final List<?> subjects) {
        return (null == subjects) ? null : subjects.stream().map(NormalizerTest::expected).collect(Collectors.toList());
    }

    private static Map<?, ?> expected(final Map<?, ?> subjects) {
        return (null == subjects)
                ? null
                : subjects.entrySet().stream()
                          .collect(
                                  Collectors.toMap(
                                          entry -> expected(entry.getKey()),
                                          entry -> expected(entry.getValue())));
    }

    private static Object expected(final Object subject) {
        if (subject instanceof byte[]) return expected((byte[]) subject);
        if (subject instanceof Subject) return expected((Subject) subject);
        if (subject instanceof Set) return expected((Set<?>) subject);
        if (subject instanceof List) return expected((List<?>) subject);
        if (subject instanceof Map) return expected((Map<?, ?>) subject);
        return subject;
    }

    private static List<Byte> expected(final byte[] bytes) {
        final List<Byte> result = new ArrayList<>(bytes.length);
        for (final byte value : bytes) {
            result.add(value);
        }
        return result;
    }

    private byte[] anyBytes() {
        final byte[] bytes = new byte[random.nextInt(32)+1];
        random.nextBytes(bytes);
        return bytes;
    }

    private String anyString() {
        return new BigInteger(random.nextInt(128) + 1, random).toString(Character.MAX_RADIX);
    }
}
