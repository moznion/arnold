package net.moznion.arnold.test;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class BasicValueObjectTest {
    @Test
    void shouldWorkBuilderWithBasicValueObject() {
        final String fooValue = "foo-value";
        final double barValue = 46.49;
        final int buzValue = 5963;
        final List<String> words = Arrays.asList("I'm", "lovin'", "it");
        final BasicValueObject obj = new BasicValueObjectBuilder().foo(fooValue)
                                                                  .bar(barValue)
                                                                  .buz(buzValue)
                                                                  .qux(words)
                                                                  .build();

        assertEquals(fooValue, obj.getFoo());
        assertEquals(barValue, obj.getBar());
        assertEquals(buzValue, obj.buz);
        assertIterableEquals(words, obj.qux);
    }
}
