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
package com.github.sbt.junit.jupiter.internal.listeners;

import junit.TestRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

/**
 * @author Michael Aichler
 */
public class SummaryPrintingTestListenerTest {

    @Rule
    public final TestRunner testRunner = new TestRunner();

    @Test
    public void shouldCalculateDuration() {

        testRunner.execute(LongRunningTest.class.getName());


        Summary summary = testRunner.logger().all().stream()
                .map(Summary::parse)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(AssertionError::new);

        assertThat(summary.total, equalTo(3));
        assertThat(summary.failed, equalTo(1));
        assertThat(summary.ignored, equalTo(1));
        assertThat(summary.duration, greaterThan(0.1));
    }

    /**
     * Embedded JUnit Jupiter test.
     */
    static class LongRunningTest {

        @org.junit.jupiter.api.Test
        void test() throws Exception {
            Thread.sleep(150);
        }

        @Disabled
        @org.junit.jupiter.api.Test
        void shouldBeDisabled() {

        }

        @org.junit.jupiter.api.Test
        void shouldFail() {
            throw new AssertionError();
        }
    }

    static class Summary {

        static Pattern RX_FAILED = Pattern.compile(".*([0-9]+)\\sfailed,.*");
        static Pattern RX_TOTAL = Pattern.compile(".*([0-9]+)\\stotal,.*");
        static Pattern RX_IGNORED = Pattern.compile(".*([0-9]+)\\signored,.*");
        static Pattern RX_DURATION = Pattern.compile(".*([0-9]\\.[0-9]+)s");

        private Double duration = 0D;
        private Integer total = 0;
        private Integer ignored = 0;
        private Integer failed = 0;

        static Summary parse(String line) {

            if (!line.contains("Test run finished")) {
                return null;
            }

            Summary summary = new Summary();

            Matcher m = RX_DURATION.matcher(line);
            if (m.matches()) {
                summary.duration = Double.valueOf(m.group(1));
            }

            m = RX_FAILED.matcher(line);
            if (m.matches()) {
                summary.failed = Integer.valueOf(m.group(1));
            }

            m = RX_IGNORED.matcher(line);
            if (m.matches()) {
                summary.ignored = Integer.valueOf(m.group(1));
            }

            m = RX_TOTAL.matcher(line);
            if (m.matches()) {
                summary.total = Integer.valueOf(m.group(1));
            }

            return summary;
        }
    }
}