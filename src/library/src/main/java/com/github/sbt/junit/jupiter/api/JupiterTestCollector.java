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
package com.github.sbt.junit.jupiter.api;

import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import sbt.testing.Fingerprint;
import sbt.testing.Selector;
import sbt.testing.SuiteSelector;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectDirectory;

/**
 * Collects available tests via a {@link LauncherDiscoveryRequest}.
 *
 * @author Michael Aichler
 */
public class JupiterTestCollector {

    private final ClassLoader classLoader;
    private final URL[] runtimeClassPath;
    private final File classDirectory;

    /**
     * Executes a JUnit Jupiter launcher discovery request.
     *
     * @return A result which contains discovered test items.
     * @throws Exception If an error occurs
     */
    public Result collectTests() throws Exception {

        if (!classDirectory.exists()) {

            // prevent JUnits Launcher to trip over non-existent directory
            return Result.emptyResult();
        }

        final ClassLoader customClassLoader = new URLClassLoader(runtimeClassPath, classLoader);
        return invokeWithCustomClassLoader(customClassLoader, this::collectTests0);
    }

    /**
     * Defines the test discovery result which is evaluated by the SBT Plugin.
     *
     * @author Michael Aichler
     */
    public static class Result {

        final static Result EMPTY_RESULT = new Result();

        private List<Item> discoveredTests = new ArrayList<>();

        /**
         * @return An empty result.
         */
        public static Result emptyResult() {

            return EMPTY_RESULT;
        }

        /**
         * @return The list of discovered test items.
         */
        public List<Item> getDiscoveredTests() {

            return discoveredTests;
        }
    }

    /**
     * Describes a discovered test item.
     *
     * @author Michael Aichler
     */
    public static class Item {

        private String fullyQualifiedClassName;
        private Fingerprint fingerprint = new JupiterTestFingerprint();
        private List<Selector> selectors = new ArrayList<>();
        private boolean explicit;

        /**
         * @return The fully qualified class-name of the discovered test.
         */
        public String getFullyQualifiedClassName() {

            return fullyQualifiedClassName;
        }

        /**
         * @return The fingerprint used for this test item.
         */
        public Fingerprint getFingerprint() {

            return fingerprint;
        }

        /**
         * @return Whether this test item was explicitly specified.
         */
        public boolean isExplicit() {

            return explicit;
        }

        /**
         * @return The list of test selectors for this test item.
         */
        public Selector[] getSelectors() {

            return selectors.toArray(new Selector[0]);
        }

        @Override
        public String toString() {

            return "Item(" +
                    fullyQualifiedClassName +
                    ", " + fingerprint +
                    ", " + selectors +
                    ", " + explicit +
                    ')';
        }
    }

    /**
     * Builder for {@link JupiterTestCollector} instances.
     *
     * @author Michael Aichler
     */
    public static class Builder {

        private ClassLoader classLoader;
        private URL[] runtimeClassPath = new URL[0];
        private File classDirectory;

        /**
         * Specifies the classloader which should be used by the collector.
         *
         * @param value The classloader which should be used by the collector.
         * @return This builder.
         */
        public Builder withClassLoader(ClassLoader value) {

            this.classLoader = value;
            return this;
        }

        /**
         * Specifies the runtime classpath which should be used by the collector.
         *
         * @param value The runtime classpath which must contain the test classes,
         *      test dependencies and JUnit Jupiter dependencies.
         * @return This builder.
         */
        public Builder withRuntimeClassPath(URL[] value) {

            this.runtimeClassPath = value;
            return this;
        }

        /**
         * Specifies the class directory which should be searched by the collector.
         *
         * @param value The directory containing test classes.
         * @return This builder.
         */
        public Builder withClassDirectory(File value) {

            this.classDirectory = value;
            return this;
        }

        /**
         * Creates an instance of {@link JupiterTestCollector}.
         *
         * @return A new collector.
         */
        public JupiterTestCollector build() {

            return new JupiterTestCollector(this);
        }
    }

    /**
     * Initializes a new collector from the given builder instance.
     *
     * @param builder The builder instance.
     */
    private JupiterTestCollector(Builder builder) {

        this.runtimeClassPath = builder.runtimeClassPath;
        this.classDirectory = builder.classDirectory;
        this.classLoader = builder.classLoader;
    }

    /**
     * Executes a JUnit Jupiter test discovery and collects the result.
     *
     * @return The result of discovered tests.
     */
    private Result collectTests0() {

        Set<Path> classPathRoots = new HashSet<>();
        classPathRoots.add(Paths.get(classDirectory.getAbsolutePath()));

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClasspathRoots(classPathRoots))
                .selectors(selectDirectory(classDirectory))
                .build();

        TestPlan testPlan = LauncherFactory.create().discover(request);

        Result result = new Result();

        for (TestIdentifier rootIdentifier : testPlan.getRoots()) {

            for (TestIdentifier identifier : testPlan.getChildren(rootIdentifier)) {

                String fqn = fullyQualifiedName(identifier);
                Selector selector = new SuiteSelector();

                Item item = new Item();
                item.fullyQualifiedClassName = fqn;
                item.selectors.add(selector);
                item.explicit = false;

                result.discoveredTests.add(item);
            }
        }

        return result;
    }

    private String fullyQualifiedName(TestIdentifier testIdentifier) {

        TestSource testSource = testIdentifier.getSource().orElse(null);

        if (testSource instanceof ClassSource) {

            ClassSource classSource = (ClassSource)testSource;
            return classSource.getClassName();
        }

        if (testSource instanceof MethodSource) {

            MethodSource methodSource = (MethodSource)testSource;
            return methodSource.getClassName()
                    + '#' + methodSource.getMethodName()
                    + '(' + methodSource.getMethodParameterTypes() + ')';
        }

        return testIdentifier.getLegacyReportingName();
    }

    /**
     * Replaces the current threads context classloader before executing the
     * specified callable.
     *
     * @param classLoader The classloader which should be used.
     * @param callable The callable which is to be executed.
     * @param <T> The return type of the callable.
     * @return The value produced by executing the specified callable.
     * @throws Exception If an error occurs while executing the callable.
     */
    private <T> T invokeWithCustomClassLoader(ClassLoader classLoader, Callable<T> callable)
            throws Exception {

        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            return callable.call();
        }
        finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

}
