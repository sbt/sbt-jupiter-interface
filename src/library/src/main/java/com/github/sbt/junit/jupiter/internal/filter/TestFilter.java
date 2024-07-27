/*
 * jupiter-interface
 *
 * Copyright (c) 2017, Michael Aichler.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.sbt.junit.jupiter.internal.filter;

import com.github.sbt.junit.jupiter.internal.event.Dispatcher;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.PostDiscoveryFilter;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.platform.engine.FilterResult.excluded;
import static org.junit.platform.engine.FilterResult.included;

/**
 * Filter for tests specified via commandline argument {@code --tests=}.
 *
 * @author Michael Aichler
 */
public class TestFilter implements PostDiscoveryFilter {

    private final Set<String> testPatterns;
    private final String patternDescription;
    private final Dispatcher eventDispatcher;
    private final Map<UniqueId, FilterResult> alreadyTestedIds = new ConcurrentHashMap<>();

    TestFilter(Set<String> testFilters, Dispatcher eventDispatcher) {

        this.eventDispatcher = eventDispatcher;
        this.testPatterns = testFilters;
        this.patternDescription = testFilters.stream()
                .collect(Collectors.joining(" OR "));
    }

    /**
     * Conditionally creates a {@link TestFilter} depending on whether the
     * specified filter set contains any elements.
     *
     * @param testFilters The available glob patterns (might be empty)
     * @param eventDispatcher The event dispatcher which should be notified about filtered tests.
     * @return An optional glob filter depending on whether the pattern set
     *  contains any patterns.
     */
    public static Optional<TestFilter> create(Set<String> testFilters, Dispatcher eventDispatcher) {

        if (testFilters.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new TestFilter(testFilters, eventDispatcher));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FilterResult apply(TestDescriptor object) {

        final UniqueId id = object.getUniqueId();
        return alreadyTestedIds.computeIfAbsent(id, key -> {

            final Optional<String> testName = toTestName(object);
            final FilterResult result = testName.map(this::findMatchingResult)
                    .orElse(included("Not a leaf descriptor"));

            if (result.excluded()) {
                final String reason = result.getReason().orElse("");
                eventDispatcher.executionFiltered(object, reason);
            }

            return result;
        });
    }

    private Optional<String> toTestName(TestDescriptor object) {

        TestSource testSource = object.getSource().orElse(null);

        if (!object.isTest() || !(testSource instanceof MethodSource)) {
            return Optional.empty();
        }

        MethodSource methodSource = (MethodSource)testSource;
        return Optional.of(methodSource.getClassName()
                + '#' + methodSource.getMethodName()
                + '(' + methodSource.getMethodParameterTypes() + ')'
        );
    }

    FilterResult findMatchingResult(String testName) {

        return findMatchingPattern(testName)
                .map(pattern -> included("Pattern <" + pattern + "> matches <" + testName + ">"))
                .orElseGet(() -> excluded("Does not match pattern " + patternDescription));

    }

    Optional<String> findMatchingPattern(String testName) {

        return testPatterns.stream()
                .filter(p -> Pattern.matches(p, testName))
                .findAny();
    }
}
