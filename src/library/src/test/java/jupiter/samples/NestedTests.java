package jupiter.samples;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NestedTests {


    @Nested
    class First {

        @Test
        void testOfFirstNestedClass() {
        }
    }

    @Nested
    class Second {

        @Test
        void testOfSecondNestedClass() {
        }
    }
}
