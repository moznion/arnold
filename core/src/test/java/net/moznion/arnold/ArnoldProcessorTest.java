package net.moznion.arnold;

import com.google.common.io.Resources;
import com.google.testing.compile.JavaFileObjects;
import net.moznion.arnold.processor.ArnoldProcessor;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

class ArnoldProcessorTest {
    @Test
    void shouldGeneratedCodeBeValid() {
        final ArnoldProcessor processor = new ArnoldProcessor();

        assert_().about(javaSource())
                 .that(JavaFileObjects.forResource(Resources.getResource("TestCase.java")))
                 .processedWith(processor)
                 .compilesWithoutError();
    }

    @Test
    void shouldGeneratedCodeBeValidWithSingleFieldClass() {
        final ArnoldProcessor processor = new ArnoldProcessor();

        assert_().about(javaSource())
                 .that(
                     JavaFileObjects.forResource(Resources.getResource("SingleFieldTestCase.java"))
                 )
                 .processedWith(processor)
                 .compilesWithoutError();
    }

    @Test
    void shouldGeneratedCodeBeValidWithInnerClass() {
        final ArnoldProcessor processor = new ArnoldProcessor();

        assert_().about(javaSource())
                 .that(
                     JavaFileObjects.forResource(Resources.getResource("InnerClassTestCase.java"))
                 )
                 .processedWith(processor)
                 .compilesWithoutError();
    }
}
