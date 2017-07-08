package net.aichler.jupiter.internal.experimental;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.UniqueId.Segment;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Aichler
 */
public class DefaultTestNameGenerator implements TestNameGenerator {

    /**
     *
     * @param testPlan The current test plan.
     * @param identifier The test identifier from which to generate a test name.
     * @return The test name of the specified identifier.
     */
    public TestName of(TestPlan testPlan, TestIdentifier identifier) {

        DefaultTestName.Builder builder = new DefaultTestName.Builder();
        builder.withDisplayName(identifier.getDisplayName());

        for (TestIdentifier p : toPath(testPlan, identifier)) {

            Segment segment = toLastSegment(p);
            DefaultTestName.NameBuilder nameBuilder = toNameBuilder(builder, p, segment);
            nameBuilder.addTo(builder);
        }

        return builder.build();
    }

    public TestName of(String fullyQualifiedName) {

        DefaultTestName.Builder builder = new DefaultTestName.Builder();

        String className = fullyQualifiedName;
        String methodName = null;

        String[] classNameAndMethod = className.split("#");
        if (classNameAndMethod.length == 2) {
            methodName = classNameAndMethod[1];

        } else if (classNameAndMethod.length > 2) {
            throw new RuntimeException("Invalid class name " + className);
        }

        String[] classWithNested = classNameAndMethod[0].split("\\$");
        if (classWithNested.length > 1) {

        }

        return builder.build();
    }

    private Segment toLastSegment(TestIdentifier identifier) {

        String id = identifier.getUniqueId();
        return UniqueId.parse(id).getSegments().stream().reduce((a, b) -> b)
                .orElseThrow(() -> new RuntimeException("Segment not found " + identifier));
    }

    private DefaultTestName.NameBuilder toNameBuilder(DefaultTestName.Builder parent, TestIdentifier identifier, Segment segment) {

        DefaultTestName.NameBuilder builder;
        String type = segment.getType().toLowerCase();

        switch (type) {
            case "class":
                builder = new DefaultTestName.DefaultClassNameBuilder();
                break;
            case "nested-class":
                builder = new DefaultTestName.DefaultNestedClassNameBuilder();
                break;
            case "method":
                builder = new DefaultTestName.DefaultMethodNameBuilder();
                break;
            case "test-factory":
                builder = new DefaultTestName.DefaultMethodNameBuilder();
                parent.withFactory(true);
                break;
            case "test-template":
                builder = new DefaultTestName.DefaultMethodNameBuilder();
                parent.withTemplate(true);
                break;
            case "dynamic-test":
                builder = new DefaultTestName.DefaultMethodNameBuilder();
                parent.withDynamic(true);
                break;
            case "test-template-invocation":
                builder = new DefaultTestName.DefaultInvocationBuilder();
                break;
            default:
                builder = new DefaultTestName.DefaultRemainingNameBuilder();
                break;
        }

        builder.withDisplayName(identifier.getDisplayName());
        builder.withTechnicalName(segment.getValue());
        return builder;
    }

    private List<TestIdentifier> toPath(TestPlan testPlan, TestIdentifier identifier) {

        List<TestIdentifier> identifierPath = new ArrayList<>();

        while (null != identifier) {
            if (identifier.getParentId().isPresent()) {
                identifierPath.add(identifier);
            }
            identifier = testPlan.getParent(identifier).orElse(null);
        }

        Collections.reverse(identifierPath);
        return identifierPath;
    }
}
