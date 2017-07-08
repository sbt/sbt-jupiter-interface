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
package net.aichler.jupiter.internal.options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.platform.engine.UniqueId.Segment;

/**
 * Defines available commandline options.
 *
 * @author Michael Aichler
 */
public class Options {

    private final static String DISPATCH_EVENTS_TRACE_PATH = "target/jupiterDispatchEvents.log";

    private final boolean assertLogEnabled;
    private final boolean colorsEnabled;
    private final boolean decodeScalaNames;
    private final boolean exceptionClassLogEnabled;
    private final boolean quiet;
    private final boolean verbose;
    private final boolean traceDispatchEvents;
    private final boolean typesEnabled;
    private final Set<String> testFilters;
    private final List<String> includeTags;
    private final List<String> excludeTags;
    private final Set<String> globPatterns;
    private final Map<String,String> systemProperties;
    private final String displayMode;
    private final String runListener;


    /**
     * @param builder The builder instance.
     */
    private Options(Builder builder) {

        assertLogEnabled = builder.assertLogEnabled;
        colorsEnabled = builder.colorsEnabled;
        decodeScalaNames = builder.decodeScalaNames;
        exceptionClassLogEnabled = builder.exceptionClassLogEnabled;
        excludeTags = builder.excludeTags;
        globPatterns = builder.globPatterns;
        includeTags = builder.includeTags;
        quiet = builder.quiet;
        runListener = builder.runListener;
        systemProperties = builder.systemProperties;
        testFilters = builder.testFilters;
        verbose = builder.verbose;
        traceDispatchEvents = builder.traceDispatchEvents;
        typesEnabled = builder.typesEnabled;
        displayMode = builder.displayMode;
    }

    /**
     * @return {@code True}, if colored output should be enabled.
     */
    public boolean isColorsEnabled() {

        return colorsEnabled;
    }

    /**
     * @return {@code True}, if should be decoded.
     */
    public boolean isDecodeScalaNames() {

        return decodeScalaNames;
    }

    /**
     * @return {@code True}, if output of tests should be send to the debug log
     *      instead of the info log.
     */
    public boolean isQuiet() {

        return quiet;
    }

    /**
     * @return {@code True}, if verbose messages should be logged. Otherwise only
     *      errors and ignored tests should be logged.
     */
    public boolean isVerbose() {

        return verbose;
    }

    /**
     * @return {@code True}, if the class name and stacktrace of assertion errors
     *      should be logged.
     */
    public boolean isAssertLogEnabled() {

        return assertLogEnabled;
    }

    /**
     * @return {@code True}, if the class name of an exception should be logged.
     */
    public boolean isExceptionClassLogEnabled() {

        return exceptionClassLogEnabled;
    }

    /**
     * @return {@code True}, if the type for every {@link Segment} should be logged.
     */
    public boolean isTypesEnabled() {

        return typesEnabled;
    }

    /**
     * @return The display type ('flat', 'tree')
     */
    public String getDisplayMode() {

        return displayMode;
    }

    /**
     * @return An optional test run listener.
     */
    public Optional<String> getRunListener() {

        return Optional.ofNullable(runListener);
    }

    /**
     * @return The set of test name filters (might be empty).
     */
    public Set<String> getTestFilters() {

        return testFilters;
    }

    /**
     * @return An optional path to a file where dispatch events should be logged.
     */
    public Optional<String> getDispatchEventsTracePath() {

        return Optional.ofNullable(traceDispatchEvents ? DISPATCH_EVENTS_TRACE_PATH : null);
    }

    /**
     * @return The set of tags which should be included (might be empty).
     */
    public List<String> getIncludeTags() {

        return includeTags;
    }

    /**
     * @return The set of tags which should be excluded (might be empty).
     */
    public List<String> getExcludeTags() {

        return excludeTags;
    }

    /**
     * @return The set of glob patterns for test names which should be executed
     *      (might be empty).
     */
    public Set<String> getGlobPatterns() {

        return globPatterns;
    }

    /**
     * @return A list of system properties to be set before execution.
     */
    public Map<String, String> getSystemProperties() {

        return systemProperties;
    }


    /**
     *
     * @author Michael Aichler
     */
    static class Builder {

        private boolean quiet = false;
        private boolean verbose = true;
        private boolean colorsEnabled = true;
        private boolean decodeScalaNames = false;
        private boolean assertLogEnabled = true;
        private boolean exceptionClassLogEnabled = true;
        private boolean traceDispatchEvents = false;
        private boolean typesEnabled = false;
        private Set<String> testFilters = new HashSet<>();
        private List<String> includeTags = new ArrayList<>();
        private List<String> excludeTags = new ArrayList<>();
        private Set<String> globPatterns = new HashSet<>();
        private Map<String,String> systemProperties = new HashMap<>();
        private String displayMode = "flat";
        private String runListener;


        Builder withQuiet(boolean value) {

            this.quiet = value;
            return this;
        }

        Builder withVerbose(boolean value) {

            this.verbose = value;
            return this;
        }

        Builder withColorsEnabled(boolean value) {

            this.colorsEnabled = value;
            return this;
        }

        Builder withDecodeScalaNames(boolean value) {

            this.decodeScalaNames = value;
            return this;
        }

        Builder withAssertLogEnabled(boolean value) {

            this.assertLogEnabled = value;
            return this;
        }

        Builder withExceptionClassLogEnabled(boolean value) {

            this.exceptionClassLogEnabled = value;
            return this;
        }

        Builder withDisplayMode(String value) {

            this.displayMode = value;
            return this;
        }

        Builder withTraceDispatchEvents(boolean value) {

            this.traceDispatchEvents = value;
            return this;
        }

        Builder withTestFilters(Set<String> value) {

            this.testFilters.addAll(value);
            return this;
        }

        Builder withTypesEnabled(boolean value) {

            this.typesEnabled = value;
            return this;
        }

        Builder withIncludeTags(Set<String> value) {

            this.includeTags.addAll(value);
            return this;
        }

        Builder withExcludeTags(Set<String> value) {

            this.excludeTags.addAll(value);
            return this;
        }

        Builder withRunListener(String value) {

            this.runListener = value;
            return this;
        }

        Builder withGlobPattern(String value) {

            this.globPatterns.add(value);
            return this;
        }

        Builder withSystemProperty(Map.Entry<String, String> entry) {

            this.systemProperties.put(entry.getKey(), entry.getValue());
            return this;
        }

        public Options build() {

            return new Options(this);
        }
    }
}
