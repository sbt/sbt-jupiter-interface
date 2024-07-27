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
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.UniqueId.Segment;
import org.junit.platform.launcher.PostDiscoveryFilter;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.platform.engine.FilterResult.excluded;
import static org.junit.platform.engine.FilterResult.included;

/**
 * Filters test descriptors against a given set of glob patterns.
 *
 * @author Michael Aichler
 */
public class GlobFilter implements PostDiscoveryFilter {

    private final Dispatcher eventDispatcher;
    private final List<Pattern> patterns;

    GlobFilter(Set<String> globPatterns, Dispatcher eventDispatcher) {

        this.eventDispatcher = eventDispatcher;
        this.patterns = globPatterns.stream().map(this::compile)
                .collect(Collectors.toList());
    }

    /**
     * Conditionally creates a {@link GlobFilter} depending on whether the
     * specified pattern set contains any elements.
     *
     * @param globPatterns The available glob patterns (might be empty)
     * @param eventDispatcher The event dispatcher which should be notified about filtered tests.
     * @return An optional glob filter depending on whether the pattern set
     *  contains any patterns.
     */
    public static Optional<GlobFilter> create(Set<String> globPatterns, Dispatcher eventDispatcher) {

        if (globPatterns.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new GlobFilter(globPatterns, eventDispatcher));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FilterResult apply(TestDescriptor object) {

        String plainName = toTestName(object.getUniqueId());

        return findMatchingPattern(plainName)
                .map(pattern -> included(""))
                .orElseGet(() -> {
                    eventDispatcher.executionFiltered(object, "because");
                    return excluded("");
                });
    }

    /**
     * Converts the specified unique id to a test name
     *
     * @param id The unique identifier.
     * @return The converted test name.
     */
    String toTestName(UniqueId id) {

        return id.getSegments().stream()
                .skip(1)
                .map(Segment::getValue)
                .collect(Collectors.joining("."));
    }

    /**
     * Tries to match any of the compiled patterns against the specified {@code testName}.
     *
     * @param testName The test name which is to be matched.
     * @return The first pattern which matches the specified {@code testName}.
     */
    Optional<Pattern> findMatchingPattern(String testName) {

        return patterns.stream()
                .filter(pattern -> pattern.matcher(testName).matches())
                .findAny();
    }

    /**
     * Compiles the specified string representation of a glob pattern into a {@link Pattern}.
     *
     * @param glob The glob pattern which is to be compiled.
     * @return The compiled pattern.
     */
    private Pattern compile(String glob) {

        String[] a = glob.split("\\*", -1);
        StringBuilder b = new StringBuilder();

        for (int i = 0; i < a.length; i++) {

            if (0 != i) {
                b.append(".*");
            }

            if (!a[i].isEmpty()) {
                b.append(Pattern.quote(a[i].replaceAll("\n", "\\n")));
            }
        }

        return Pattern.compile(b.toString());
    }
}
