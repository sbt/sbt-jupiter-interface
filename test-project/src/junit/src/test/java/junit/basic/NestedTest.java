package junit.basic;

import org.junit.experimental.runners.Enclosed;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class NestedTest {

    public static class First {

        @Test
        public void testInFirstNestedClass() {

            System.out.println("Invoking " + getClass().getName() + "#testInFirstNestedClass()");
        }
    }

    public static class Second {

        @Test
        public void testInSecondNestedClass() {

            System.out.println("Invoking " + getClass().getName() + "#testInSecondNestedClass()");
        }
    }
}
