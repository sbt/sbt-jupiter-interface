package vintage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertEquals;

/**
 * Sample test methods for the vintage-engine using {@link Parameterized} runner.
 */
@RunWith(Parameterized.class)
public class ParameterizedTests {

    private final char alpha;
    private final int code;

    public ParameterizedTests(char alpha, int number) {

        this.alpha = alpha;
        this.code = number;
    }

    @Parameters
    public static Object[][] data() {
        return new Object[][] {
                { 'A', 65 },
                { 'B', 66 },
                { 'C', 67 }
        };
    }

    @Test
    public void testParameters() {

        assertEquals((int)alpha, code);
    }
}
