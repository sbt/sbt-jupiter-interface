package jupiter;

import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.util.Collections;
import java.util.Set;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * @author Michael Aichler
 */
public class TestHelper {

    public static TestPlan fromClassName(String className) {

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(className))
                .build();

        return LauncherFactory.create().discover(request);
    }

    public static Set<TestIdentifier> descendantsOfFirstRoot(TestPlan testPlan) {

        return testPlan.getRoots().stream().findAny()
                .map(testPlan::getDescendants)
                .orElseGet(Collections::emptySet);
    }

    public static TestIdentifier findByName(TestPlan testPlan, String testName) {

        return descendantsOfFirstRoot(testPlan).stream()
                .filter(identifier -> testName.equals(identifier.getLegacyReportingName()))
                .findAny().orElseThrow(AssertionError::new);
    }
}
