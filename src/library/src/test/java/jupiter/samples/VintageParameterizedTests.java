package jupiter.samples;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Sample test methods for the vintage-engine using {@link Parameterized} runner.
 */
@RunWith(Parameterized.class)
public class VintageParameterizedTests {

    private final char alpha;
    private final int code;

    public VintageParameterizedTests(char alpha, int number) {
        this.alpha = alpha;
        this.code = number;
    }

    @Parameters(name = "{0}-{1}")
    public static Object[][] data() {
        return new Object[][] {
                { 'A', 65 },
                { 'B', 66 },
                { 'C', 67 }
        };
    }

    @Test
    public void testParameters() {
        assertThat((int)alpha, equalTo(code));
    }
}
