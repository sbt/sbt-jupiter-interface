package jupiter.basic;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class FooTest {

    @Test
    void testFoo() {

        System.out.println("Invoking " + getClass().getName() + "#testFoo()");
    }

    @Test
    @Disabled("Because I am disabled")
    void disabledTest() {

    }

    @Test
    void failingTest() {

        System.out.println("Invoking " + getClass().getName() + "#failingTest()");
        throw new AssertionError("Because I am supposed to fail");
    }
}
