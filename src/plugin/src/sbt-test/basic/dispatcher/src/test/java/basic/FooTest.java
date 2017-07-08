package basic;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class FooTest {

    @Test
    void testFoo() {

    }

    @Test
    @Disabled("Because I am disabled")
    void disabledTest() {

    }

    @Test
    void failingTest() {
        System.out.println("Output before assertion fails");
        throw new AssertionError("Because I am supposed to fail");
    }
}
