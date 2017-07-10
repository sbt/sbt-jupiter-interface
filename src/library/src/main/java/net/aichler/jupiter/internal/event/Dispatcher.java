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
package net.aichler.jupiter.internal.event;

import net.aichler.jupiter.api.JupiterTestFingerprint;
import net.aichler.jupiter.api.JupiterTestListener;
import net.aichler.jupiter.internal.Configuration;
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

    private final Configuration configuration;
    private final EventHandler eventHandler;
    private final Fingerprint fingerprint = new JupiterTestFingerprint();
    private final Map<TestIdentifier,Boolean> reportedIds = new ConcurrentHashMap<>();
    private final Map<TestIdentifier,Long> startTimes = new ConcurrentHashMap<>();

    public Dispatcher(Configuration configuration, EventHandler eventHandler) {

        this.configuration = configuration;
        this.eventHandler = eventHandler;
    }

    @Override
    public void executionSkipped(TestIdentifier identifier, String reason) {

        reportedIds.computeIfAbsent(identifier, key -> {

            final long duration = calculateDuration(key);
            final String taskName = configuration.fullyQualifiedTaskName(identifier);
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

        eventHandler.handle(new DispatchEvent(className, Status.Error, 0L, throwable));
    }

    @Override
    public void executionFiltered(TestDescriptor descriptor, String reason) {

        final String fqn = configuration.fullyQualifiedTaskName(TestIdentifier.from(descriptor));
        eventHandler.handle(new DispatchEvent(fqn, Status.Skipped, 0L));
    }

    @Override
    public void executionFinished(TestIdentifier identifier, TestExecutionResult result) {

        reportedIds.computeIfAbsent(identifier, key -> {

            final Status status;
            final Throwable throwable = result.getThrowable().orElse(null);
            final String taskName = configuration.fullyQualifiedTaskName(identifier);
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

    class DispatchEvent implements Event {

        final Status status;
        final Throwable throwable;
        final long duration;
        final String className;
        final String methodName;
        final Selector selector;

        DispatchEvent(String fqn, Status status, long duration) {
            this(fqn, status, duration, null);
        }

        DispatchEvent(String fqn, Status status, long duration, Throwable throwable) {

            this.status = status;
            this.throwable = throwable;
            this.duration = duration;

            int indexOfHash = fqn.indexOf('#');
            if (-1 == indexOfHash) {
                className = fqn;
                methodName = null;
                selector = new SuiteSelector();
            } else {
                className = fqn.substring(0, indexOfHash);
                methodName = fqn.substring(indexOfHash + 1);
                selector = new TestSelector(methodName);
            }
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
