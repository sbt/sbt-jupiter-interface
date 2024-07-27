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
package junit;

import com.github.sbt.junit.jupiter.api.JupiterTestFingerprint;
import com.github.sbt.junit.jupiter.api.StreamPair;
import com.github.sbt.junit.jupiter.internal.JupiterRunner;
import org.junit.rules.ExternalResource;
import sbt.testing.Event;
import sbt.testing.EventHandler;
import sbt.testing.Logger;
import sbt.testing.Selector;
import sbt.testing.Status;
import sbt.testing.SuiteSelector;
import sbt.testing.Task;
import sbt.testing.TaskDef;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Test rule which executes tests through {@link JupiterRunner}.
 *
 * @author Michael Aichler
 */
public class TestRunner extends ExternalResource {


    private String[] args = new String[0];
    private String[] remoteArgs = new String[0];
    private DummyLogger logger = new DummyLogger();
    private ClassLoader classLoader = getClass().getClassLoader();
    private DummyEventHandler eventHandler = new DummyEventHandler();
    private static final StreamPair streamPair = new StreamPair(System.out, System.err);

    /**
     *
     * @param values The commandline arguments.
     * @return This rule.
     */
    public TestRunner withArgs(String... values) {

        args = Objects.requireNonNull(values);
        return this;
    }

    /**
     *
     * @param values The remote arguments.
     * @return This rule.
     */
    public TestRunner withRemoteArgs(String... values) {

        remoteArgs = Objects.requireNonNull(values);
        return this;
    }

    /**
     * @return The dummy event handler.
     */
    public DummyEventHandler eventHandler() {

        return eventHandler;
    }

    /**
     * @return The dummy logger.
     */
    public DummyLogger logger() {

        return logger;
    }

    /**
     * Execute {@link JupiterRunner} with the specified test class.
     *
     * @param clazz The test class which is to be run.
     */
    public void execute(Class<?> clazz) {

        execute(clazz.getName());
    }

    /**
     * Execute {@link JupiterRunner} for a test matching {@code fullyQualifiedClassName}.
     *
     * @param fullyQualifiedClassName The fully qualified name of the test class.
     */
    public void execute(String fullyQualifiedClassName) {


        JupiterRunner runner = new JupiterRunner(args, remoteArgs, classLoader, streamPair);
        Task[] tasks = runner.tasks(new TaskDef[]{ createTaskDef(fullyQualifiedClassName) });
        tasks[0].execute(eventHandler, new Logger[] { logger });
    }

    private TaskDef createTaskDef(String fullyQualifiedName) {

        Selector[] selectors = new Selector[]{ new SuiteSelector() };
        return new TaskDef(fullyQualifiedName, new JupiterTestFingerprint(), false, selectors);
    }

    /**
     * @author Michael Aichler
     */
    public static class DummyEventHandler implements EventHandler {

        final List<Event> events = new ArrayList<>();

        /**
         * @return The list of received events.
         */
        public List<Event> all() {
            return events;
        }

        /**
         *
         * @param status The status by which the events should be filtered.
         * @return A list of events matching the given status.
         */
        public List<Event> byStatus(Status status) {

            return events.stream()
                    .filter(e -> status.equals(e.status()))
                    .collect(Collectors.toList());
        }

        @Override
        public void handle(Event event) {

            events.add(event);
        }
    }

    /**
     * @author Michael Aichler
     */
    public static class DummyLogger implements Logger {

        public final List<String> entries = new ArrayList<>();

        /**
         * @return All log entries received by this logger.
         */
        public List<String> all() {

            return entries;
        }

        /**
         *
         * @param level The log level (debug,error,info,warn)
         * @return The list of entries matching the specified level.
         */
        public List<String> byLevel(String level) {

            String fullLevel = '[' + level + ']';
            return entries.stream()
                    .filter(e -> e.startsWith(fullLevel))
                    .collect(Collectors.toList());
        }

        @Override
        public boolean ansiCodesSupported() {

            return false;
        }

        @Override
        public void error(String msg) {

            entries.add("[error] " + msg);
        }

        @Override
        public void warn(String msg) {

            entries.add("[warn] " + msg);
        }

        @Override
        public void info(String msg) {

            entries.add("[info] " + msg);
        }

        @Override
        public void debug(String msg) {

            entries.add("[debug] " + msg);
        }

        @Override
        public void trace(Throwable t) {

        }
    }
}
