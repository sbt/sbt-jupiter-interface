package net.aichler.jupiter.internal.experimental;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Michael Aichler
 */
public class DefaultTestName implements TestName {

    private final Name className;
    private final Name methodName;
    private final List<Name> nestedClassNames;
    private final Name methodParameterTypes;
    private final Name invocation;
    private final List<Name> remaining;
    private final String displayName;
    private final boolean isFactory;
    private final boolean isDynamic;
    private final boolean isTemplate;

    /**
     * {@inheritDoc}
     */
    @Override
    public Name className() {

        return className;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Name> nestedClassNames() {

        return nestedClassNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Name> methodName() {

        return Optional.ofNullable(methodName);
    }

    @Override
    public Optional<Name> methodParameterTypes() {

        return Optional.ofNullable(methodParameterTypes);
    }

    @Override
    public Optional<Name> invocation() {

        return Optional.ofNullable(invocation);
    }

    @Override
    public List<Name> remaining() {

        return remaining;
    }

    @Override
    public String displayName() {

        return displayName;
    }

    @Override
    public boolean isDynamic() {

        return isDynamic;
    }

    @Override
    public boolean isFactory() {

        return isFactory;
    }

    @Override
    public boolean isTemplate() {

        return isTemplate;
    }

    @Override
    public String toString() {

        return technicalName();
    }

    private DefaultTestName(Builder builder) {

        className = Objects.requireNonNull(builder.className, "Class name must not be null");
        displayName = Objects.requireNonNull(builder.displayName, "Display name must not be null");
        nestedClassNames = Collections.unmodifiableList(builder.nestedClassNames);
        methodName = builder.methodName;
        methodParameterTypes = builder.methodParameterTypes;
        invocation = builder.invocation;
        remaining = builder.remaining;
        isDynamic = builder.isDynamic;
        isFactory = builder.isFactory;
        isTemplate = builder.isTemplate;
    }

    static class Builder {

        private Name className;
        private Name methodName;
        private Name methodParameterTypes;
        private List<Name> nestedClassNames = new ArrayList<>();
        private List<Name> remaining = new ArrayList<>();
        private Name invocation;
        private String displayName;
        private boolean isDynamic;
        private boolean isFactory;
        private boolean isTemplate;

        Builder withClassName(Name value) {

            className = value;
            return this;
        }

        Builder withMethodName(Name value) {

            methodName = value;
            return this;
        }

        Builder withMethodParameterTypes(Name value) {

            methodParameterTypes = value;
            return this;
        }

        Builder withNestedClassName(Name value) {

            nestedClassNames.add(value);
            return this;
        }

        Builder withInvocation(Name value) {

            invocation = value;
            return this;
        }

        Builder withRemaining(Name value) {

            remaining.add(value);
            return this;
        }

        Builder withDisplayName(String value) {

            displayName = value;
            return this;
        }

        Builder withDynamic(boolean value) {

            isDynamic = value;
            return this;
        }

        Builder withFactory(boolean value) {

            isFactory = value;
            return this;
        }

        Builder withTemplate(boolean value) {

            isTemplate = value;
            return this;
        }

        TestName build() {
            return new DefaultTestName(this);
        }
    }

    static class DefaultName implements Name {

        private final String displayName;
        private final String technicalName;

        private DefaultName(NameBuilder builder) {
            displayName = builder.displayName;
            technicalName = builder.technicalName;
        }

        @Override
        public String displayName() {

            return displayName;
        }

        @Override
        public String technicalName() {

            return technicalName;
        }
    }

    static abstract class NameBuilder {

        private String displayName;
        private String technicalName;

        abstract void addTo(Builder builder);

        NameBuilder withDisplayName(String value) {

            displayName = value;
            return this;
        }

        NameBuilder withTechnicalName(String value) {

            technicalName = value;
            return this;
        }

        public Name build() {
            return new DefaultName(this);
        }

    }

    static class DefaultClassNameBuilder extends NameBuilder {

        @Override
        public void addTo(DefaultTestName.Builder builder) {

            builder.withClassName(build());
        }
    }

    static class DefaultNestedClassNameBuilder extends NameBuilder {

        @Override
        public void addTo(DefaultTestName.Builder builder) {

            builder.withNestedClassName(build());
        }
    }

    static class DefaultMethodNameBuilder extends NameBuilder {

        @Override
        public void addTo(DefaultTestName.Builder builder) {

            builder.withMethodName(build());
        }
    }

    static class DefaultMethodParameterTypesBuilder extends NameBuilder {

        @Override
        public void addTo(DefaultTestName.Builder builder) {

            builder.withMethodParameterTypes(build());
        }
    }

    static class DefaultInvocationBuilder extends NameBuilder {

        @Override
        public void addTo(DefaultTestName.Builder builder) {

            builder.withInvocation(build());
        }
    }

    static class DefaultRemainingNameBuilder extends NameBuilder {

        @Override
        public void addTo(DefaultTestName.Builder builder) {

            builder.withRemaining(build());
        }
    }
}
