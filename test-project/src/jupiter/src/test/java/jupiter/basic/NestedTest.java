package jupiter.basic;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NestedTest {

    @Nested
    class First {

        @Test
        void testInFirstNestedClass() {

            System.out.println("Invoking " + getClass().getName() + "#testInFirstNestedClass()");
        }
    }

    @Nested
    class Second {

        @Test
        void testInSecondNestedClass() {

            System.out.println("Invoking " + getClass().getName() + "#testInSecondNestedClass()");
        }
    }
}
