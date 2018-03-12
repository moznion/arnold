package net.moznion.arnold.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InnerClassObjectTest {
    @Test
    void shouldWorkBuilderWithInnerClassObject() {
        final InnerClassObject obj = new InnerClassObjectBuilder()
            .inner1(new Inner1Builder().foo("foo1").build())
            .inner2(new Inner2Builder().foo("foo2").build())
            .foo("foo")
            .build();

        assertEquals("foo", obj.getFoo());
        assertEquals("foo1", obj.getInner1().getFoo());
        assertEquals("foo2", obj.getInner2().getFoo());
    }
}
