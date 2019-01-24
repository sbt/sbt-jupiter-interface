package jupiter;

import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Michael Aichler
 */
public class TestHelper {

    /**
     * Creates a new test plan helper for a specific test class.
     *
     * @param testClass The name of the test class.
     * @return A new test helper instance.
     */
    public static TestHelper of(String testClass) {

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(testClass))
                .build();

        final TestPlan testPlan = LauncherFactory.create().discover(request);

        return new TestHelper(testPlan);
    }

    public TestIdentifier findByMethodName(String testMethodName) {

        final Set<TestIdentifier> descendantIdentifiers = getAllDescendants();
        return descendantIdentifiers.stream()
                .filter(testIdentifier -> {

                    TestSource testSource = testIdentifier.getSource().orElse(null);
                    if (testSource instanceof MethodSource) {
                        return ((MethodSource) testSource).getMethodName().equals(testMethodName);
                    }
                    return false;
                })
                .findAny()
                .orElseThrow(() -> {
                    String message = "Could not find test method " + testMethodName + " in:\n";
                    String identifiers = descendantIdentifiers.stream()
                            .map(TestIdentifier::getUniqueId)
                            .collect(Collectors.joining(",\n"));

                    return new AssertionError(message + identifiers);
                });
    }

    public TestPlan testPlan() {
        return testPlan;
    }

    final private TestPlan testPlan;

    private TestHelper(TestPlan testPlan) {

        this.testPlan = testPlan;
    }

    /**
     * @return Descendant test identifiers from all available roots (test engines).
     */
    private Set<TestIdentifier> getAllDescendants() {

        return testPlan.getRoots().stream()
                .map(r -> testPlan().getDescendants(r).stream())
                .flatMap(s -> s)
                .collect(Collectors.toSet());
    }
}
