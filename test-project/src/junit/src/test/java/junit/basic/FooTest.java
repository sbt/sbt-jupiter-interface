package junit.basic;

import org.junit.Ignore;
import org.junit.Test;

public class FooTest {

    @Test
    public void testFoo() {

        System.out.println("Invoking " + getClass().getName() + "#testFoo()");
    }

    @Test
    @Ignore("Because I am disabled")
    public void disabledTest() {

    }

    @Test
    public void failingTest() {

        System.out.println("Invoking " + getClass().getName() + "#failingTest()");
        throw new AssertionError("Because I am supposed to fail");
    }
}
