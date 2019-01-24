package jupiter.basic;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class DynamicTests {

    @TestFactory
    Collection<DynamicTest> dynamicTestsFromCollection() {
        return Arrays.asList(
                dynamicTest("1st dynamic test", () -> {
                    System.out.println("Invoking " + getClass().getName() + "#dynamicTestsFromCollection:1");
                    assertTrue(true);
                }),
                dynamicTest("2nd dynamic test", () -> {
                    System.out.println("Invoking " + getClass().getName() + "#dynamicTestsFromCollection:2");
                    assertEquals(4, 2 * 2);
                })
        );
    }
}
