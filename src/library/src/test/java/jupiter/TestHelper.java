package jupiter;

import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Michael Aichler
 */
public class TestHelper {

    private TestPlan testPlan;

    public Set<TestIdentifier> descendantsOfFirstRoot() {

        return testPlan.getRoots().stream().findAny()
                .map(testPlan::getDescendants)
                .orElseGet(Collections::emptySet);
    }

    public TestIdentifier findByName(String testName) {

        return descendantsOfFirstRoot().stream()
                .filter(identifier -> testName.equals(identifier.getLegacyReportingName()))
                .findAny().orElseThrow(() -> {
                    String message = "Could not find test " + testName + " in:\n";
                    String identifiers = descendantsOfFirstRoot().stream()
                            .map(TestIdentifier::getLegacyReportingName)
                            .collect(Collectors.joining("\n"));
                    return new AssertionError(message + identifiers);
                });
    }

    public TestHelper loadTestClass(String className) {

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(className))
                .build();

        testPlan = LauncherFactory.create().discover(request);
        return this;
    }

    /**
     * @see #loadTestClass(String)
     * @return The test plan
     */
    public TestPlan testPlan() {
        return testPlan;
    }

}
