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
package com.github.sbt.junit.jupiter.internal.event;

import com.github.sbt.junit.jupiter.api.JupiterTestFingerprint;
import com.github.sbt.junit.jupiter.api.JupiterTestListener;
import com.github.sbt.junit.jupiter.internal.Configuration;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestIdentifier;
import sbt.testing.Event;
import sbt.testing.EventHandler;
import sbt.testing.Fingerprint;
import sbt.testing.OptionalThrowable;
import sbt.testing.Selector;
import sbt.testing.Status;
import sbt.testing.SuiteSelector;
import sbt.testing.TestSelector;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dispatches test events to SBT.
 *
 * @author Michael Aichler
 */
public class Dispatcher implements JupiterTestListener {

    private final static Fingerprint fingerprint = new JupiterTestFingerprint();

    private final EventHandler eventHandler;
    private final Map<TestIdentifier,Boolean> reportedIds = new ConcurrentHashMap<>();
    private final Map<TestIdentifier,Long> startTimes = new ConcurrentHashMap<>();
    private final String testSuiteName;

    public Dispatcher(Configuration configuration, EventHandler eventHandler) {

        this.eventHandler = eventHandler;
        this.testSuiteName = configuration.getTestSuiteName();
    }

    @Override
    public void executionSkipped(TestIdentifier identifier, String reason) {

        reportedIds.computeIfAbsent(identifier, key -> {

            final long duration = calculateDuration(key);
            final TaskName taskName = TaskName.of(testSuiteName, identifier);
            eventHandler.handle(new DispatchEvent(taskName, Status.Skipped, duration));
            return true;
        });
    }

    @Override
    public void executionStarted(TestIdentifier identifier) {

        startTimes.computeIfAbsent(identifier, key -> System.currentTimeMillis());
    }

    @Override
    public void executionFailed(String className, Throwable throwable) {

        final TaskName taskName = TaskName.of(testSuiteName, className);
        eventHandler.handle(new DispatchEvent(taskName, Status.Error, 0L, throwable));
    }

    @Override
    public void executionFiltered(TestDescriptor descriptor, String reason) {

        final TaskName taskName = TaskName.of(testSuiteName, TestIdentifier.from(descriptor));
        eventHandler.handle(new DispatchEvent(taskName, Status.Skipped, 0L));
    }

    @Override
    public void executionFinished(TestIdentifier identifier, TestExecutionResult result) {

        reportedIds.computeIfAbsent(identifier, key -> {

            final Status status;
            final Throwable throwable = result.getThrowable().orElse(null);
            final TaskName taskName = TaskName.of(testSuiteName, identifier);
            final long duration = calculateDuration(identifier);

            // dispatch only tests by default so that number of executed tests
            // match those from junit-interface

            boolean dispatch = identifier.isTest();

            switch (result.getStatus()) {
                case ABORTED:
                    status = Status.Canceled;
                    dispatch = true;
                    break;
                case FAILED:
                    status = Status.Failure;
                    dispatch = true;
                    break;
                case SUCCESSFUL:
                    status = Status.Success;
                    break;
                default:
                    status = Status.Pending;
                    dispatch = true;
                    break;
            }

            if (dispatch) {
                eventHandler.handle(new DispatchEvent(taskName, status, duration, throwable));
            }

            return true;
        });
    }

    static class DispatchEvent implements Event {

        final Status status;
        final Throwable throwable;
        final long duration;
        final String className;
        final Selector selector;

        DispatchEvent(TaskName name, Status status, long duration) {
            this(name, status, duration, null);
        }

        DispatchEvent(TaskName name, Status status, long duration, Throwable throwable) {

            this.status = status;
            this.throwable = throwable;
            this.duration = duration;
            this.className = name.fullyQualifiedName();
            this.selector = toSelector(name);
        }

        /**
         * Converts the specified {@code taskName} to a selector.
         *
         * @param name The task name.
         * @return An appropriate selector instance.
         */
        static Selector toSelector(TaskName name) {

            String testName = name.testName();
            if (null != testName) {
                if (null != name.invocation()) {
                    testName = testName + ":" + name.invocation();
                }
            }

            if (null != name.nestedSuiteId()) {
                if (null != name.testName()) {

                    // FIXME: as soon as JUnitXmlTestsListener supports this
                    // return new NestedTestSelector(name.nestedSuiteId(), name.testName());
                    return new TestSelector(name.nestedSuiteId() + "#" + testName);
                }


                // FIXME: as soon as JUnitXmlTestsListener supports this
                // return new NestedSuiteSelector(name.nestedSuiteId());
                return new TestSelector(name.nestedSuiteId());
            }

            if (null != name.testName()) {

                return new TestSelector(testName);
            }

            return new SuiteSelector();
        }

        @Override
        public String fullyQualifiedName() {

            return className;
        }

        @Override
        public Fingerprint fingerprint() {

            return fingerprint;
        }

        @Override
        public Selector selector() {

            return selector;
        }

        @Override
        public Status status() {

            return status;
        }

        @Override
        public OptionalThrowable throwable() {

            return Optional.ofNullable(throwable).map(OptionalThrowable::new)
                    .orElseGet(OptionalThrowable::new);
        }

        @Override
        public long duration() {

            return duration;
        }

        @Override
        public String toString() {

            return "DispatchEvent(" + status +
                    ", " + className +
                    ", " + selector +
                    ')';
        }
    }

    private long calculateDuration(TestIdentifier identifier) {

        final long startTime = startTimes.getOrDefault(identifier, 0L);
        return (0L == startTime ? startTime : System.currentTimeMillis() - startTime);
    }
}
