package net.aichler.jupiter.internal.experimental;

import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * @author Michael Aichler
 */
public interface TestNameGenerator {

    /**
     *
     * @param testPlan The current test plan.
     * @param testIdentifier The test identifier from which to generate a test name.
     * @return The test name of the specified identifier.
     */
    TestName of(TestPlan testPlan, TestIdentifier testIdentifier);

}
