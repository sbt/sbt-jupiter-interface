package junit.failure;

import org.junit.Test;

import static org.junit.Assume.assumeTrue;

public class AssumptionsTest {

    @Test
    public void withAssumptionEvaluatingToFalse() {

        assumeTrue("False given", false);
    }
}
