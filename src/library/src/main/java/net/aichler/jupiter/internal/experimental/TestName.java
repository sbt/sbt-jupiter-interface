package net.aichler.jupiter.internal.experimental;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Michael Aichler
 */
public interface TestName {

    /**
     * @return The class name instance.
     */
    Name className();

    /**
     * @return An optional set of nested class name instances.
     */
    List<Name> nestedClassNames();

    /**
     * @return The method if this test name represents a test method.
     */
    Optional<Name> methodName();

    /**
     * @return The method parameter types if this test name represents a test method.
     */
    Optional<Name> methodParameterTypes();

    /**
     * @return The invocation if this test name is a dynamic test or test template.
     */
    Optional<Name> invocation();

    /**
     * @return The list of unknown remaining names.
     */
    List<Name> remaining();

    /**
     * @return The display name as reported from JUnit.
     */
    String displayName();

    boolean isDynamic();

    boolean isFactory();

    boolean isTemplate();

    /**
     * @return The technical representation of this test name.
     */
    default String technicalName() {

        StringBuilder sb = new StringBuilder(className().technicalName());

        nestedClassNames().forEach(n -> sb.append('$').append(n.technicalName()));
        methodName().ifPresent(n -> sb.append('#').append(n.technicalName()));
        methodParameterTypes().ifPresent(n -> sb.append('(').append(n.technicalName()).append(')'));
        invocation().ifPresent(n -> sb.append(':').append(n.technicalName()));
        remaining().forEach(n -> sb.append(':').append(n.technicalName()));

        return sb.toString();
    }

    interface Name {

        String displayName();
        String technicalName();
    }
}
