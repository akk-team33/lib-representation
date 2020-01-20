package de.team33.test.representation.v0;

import de.team33.libs.representation.v0.Normalizer;
import de.team33.test.representation.shared.Subject;
import org.junit.Test;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

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
                                             .setTheSubjectList(Collections.singletonList(new Subject()));
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
        result.put("theSubjectList", expected(subject.getTheSubjectList()));
        return result;
    }

    private static List<?> expected(final List<? extends Subject> subjects) {
        return (null == subjects) ? null : subjects.stream().map(NormalizerTest::expected).collect(Collectors.toList());
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
        return new BigInteger(128, random).toString(Character.MAX_RADIX);
    }
}
