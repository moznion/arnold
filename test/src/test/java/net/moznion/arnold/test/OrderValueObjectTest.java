package net.moznion.arnold.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderValueObjectTest {
    @Test
    void shouldOrderBeCorrect() {
        final OrderValueObject built = new OrderValueObjectBuilder().bar(-1)
                                                                    .foobar(-1)
                                                                    .buz(0)
                                                                    .qux(1)
                                                                    .buzqux(1)
                                                                    .foo(2)
                                                                    .xxx(999)
                                                                    .build();
        assertNotNull(built);
    }
}