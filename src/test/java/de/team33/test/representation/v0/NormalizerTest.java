package de.team33.test.representation.v0;

import com.google.common.collect.ImmutableMap;
import de.team33.libs.representation.v0.Normalizer;
import de.team33.test.representation.shared.Subject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NormalizerTest {

    private Normalizer normalizer = Normalizer.builder()
            .addMethod(Subject.class, (normalizer, subject) -> normalizer.normalFieldMap(Subject.class, subject))
            .build();

    @Test
    public void normal() {
        final Subject subject = new Subject();
        final ImmutableMap<String, Object> expected = ImmutableMap.<String, Object>builder()
                .build();
        final Object result = normalizer.normal(subject);
        assertEquals(expected, result);
    }
}