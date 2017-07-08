package jupiter.failure;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class AssumptionsTest {

    @Test
    void withAssumptionEvaluatingToFalse() {

        Assumptions.assumeTrue(false, "False given");
    }
}
