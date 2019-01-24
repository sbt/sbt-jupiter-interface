package vintage;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class EnclosedTest {

    public static class NestedTest {

        @Test
        public void someTestMethod() {

        }
    }
}
