package jupiter.samples;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

/**
 * Sample test methods for the vintage-engine.
 */
@RunWith(Enclosed.class)
public class VintageEnclosedTests {

    public static class NestedTest {

        @Test
        public void testMethod() {

        }
    }
}
