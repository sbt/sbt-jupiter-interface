import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestFoo {

    @Test
    void testFoo() {
        System.out.println("Running test " + getClass().getName());
        assertEquals(true, true, "Test should pass");
    }
}