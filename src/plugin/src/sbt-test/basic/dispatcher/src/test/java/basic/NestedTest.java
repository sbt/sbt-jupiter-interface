package basic;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NestedTest {

    @Nested
    class First {

        @Test
        void testInFirstNestedClass() {
        }
    }

    @Nested
    class Second {

        @Test
        void testInSecondNestedClass() {
        }
    }
}
