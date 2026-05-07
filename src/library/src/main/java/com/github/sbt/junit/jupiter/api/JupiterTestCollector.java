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

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectDirectory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.IntFunction;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.LauncherDiscoveryListener;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSessionListener;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import sbt.testing.Fingerprint;
import sbt.testing.Selector;
import sbt.testing.SuiteSelector;

/**
 * Collects available tests via a {@link LauncherDiscoveryRequest}.
 *
 * @author Michael Aichler
 */
public class JupiterTestCollector {

  private final ClassLoader classLoader;
  private final URL[] runtimeClassPath;
  private final File classDirectory;

  private final boolean testEngineAutoRegistrationEnabled;
  private final boolean launcherSessionListenerAutoRegistrationEnabled;
  private final boolean launcherDiscoveryListenerAutoRegistrationEnabled;
  private final boolean testExecutionListenerAutoRegistrationEnabled;
  private final boolean postDiscoveryFilterAutoRegistrationEnabled;

  private final List<String> testEngines;
  private final List<String> launcherSessionListeners;
  private final List<String> launcherDiscoveryListeners;
  private final List<String> testExecutionListeners;
  private final List<String> postDiscoveryFilters;

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

    static final Result EMPTY_RESULT = new Result();

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

      return "Item("
          + fullyQualifiedClassName
          + ", "
          + fingerprint
          + ", "
          + selectors
          + ", "
          + explicit
          + ')';
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

    private boolean testEngineAutoRegistrationEnabled = true;
    private boolean launcherSessionListenerAutoRegistrationEnabled = true;
    private boolean launcherDiscoveryListenerAutoRegistrationEnabled = true;
    private boolean testExecutionListenerAutoRegistrationEnabled = true;
    private boolean postDiscoveryFilterAutoRegistrationEnabled = true;

    private List<String> testEngines = Collections.emptyList();
    private List<String> launcherSessionListeners = Collections.emptyList();
    private List<String> launcherDiscoveryListeners = Collections.emptyList();
    private List<String> testExecutionListeners = Collections.emptyList();
    private List<String> postDiscoveryFilters = Collections.emptyList();

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
     * @param value The runtime classpath which must contain the test classes, test dependencies and
     *     JUnit Jupiter dependencies.
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
     * Configures the Jupiter Test Discovery Launcher to enable/disable auto registration of test
     * engines. Test engines need to be present on the classpath to be registered automatically.
     *
     * @param value enable/disable auto registration
     * @return This builder.
     */
    public Builder withTestEngineAutoRegistrationEnabled(boolean value) {
      this.testEngineAutoRegistrationEnabled = value;
      return this;
    }

    /**
     * Configures the Jupiter Test Discovery Launcher to enable/disable auto registration of
     * launcher session listeners. Launcher session listeners need to be present on the classpath to
     * be registered automatically.
     *
     * @param value enable/disable auto registration
     * @return This builder.
     */
    public Builder withLauncherSessionListenerAutoRegistrationEnabled(boolean value) {
      this.launcherSessionListenerAutoRegistrationEnabled = value;
      return this;
    }

    /**
     * Configures the Jupiter Test Discovery Launcher to enable/disable auto registration of
     * launcher discovery listeners. Launcher discovery listeners need to be present on the
     * classpath to be registered automatically.
     *
     * @param value enable/disable auto registration
     * @return This builder.
     */
    public Builder withLauncherDiscoveryListenerAutoRegistrationEnabled(boolean value) {
      this.launcherDiscoveryListenerAutoRegistrationEnabled = value;
      return this;
    }

    /**
     * Configures the Jupiter Test Discovery Launcher to enable/disable auto registration of test
     * execution listeners. Test execution listeners need to be present on the classpath to be
     * registered automatically.
     *
     * @param value enable/disable auto registration
     * @return This builder.
     */
    public Builder withTestExecutionListenerAutoRegistrationEnabled(boolean value) {
      this.testExecutionListenerAutoRegistrationEnabled = value;
      return this;
    }

    /**
     * Configures the Jupiter Test Discovery Launcher to enable/disable auto registration of post
     * discovery filters. Post discovery filters need to be present on the classpath to be
     * registered automatically.
     *
     * @param value enable/disable auto registration
     * @return This builder.
     */
    public Builder withPostDiscoveryFilterAutoRegistrationEnabled(boolean value) {
      this.postDiscoveryFilterAutoRegistrationEnabled = value;
      return this;
    }

    /**
     * Configures the Jupiter Test Discovery Launcher to manually register the given test engines,
     * specified by fully-qualified class name. Each class must declare a public zero-argument
     * constructor.
     *
     * @param value list of fully-qualified class names
     * @return This builder.
     */
    public Builder withTestEngines(List<String> value) {
      this.testEngines = value;
      return this;
    }

    /**
     * Configures the Jupiter Test Discovery Launcher to manually register the given launcher
     * session listeners, specified by fully-qualified class name. Each class must declare a public
     * zero-argument constructor.
     *
     * @param value list of fully-qualified class names
     * @return This builder.
     */
    public Builder withLauncherSessionListeners(List<String> value) {
      this.launcherSessionListeners = value;
      return this;
    }

    /**
     * Configures the Jupiter Test Discovery Launcher to manually register the given launcher
     * discovery listeners, specified by fully-qualified class name. Each class must declare a
     * public zero-argument constructor.
     *
     * @param value list of fully-qualified class names
     * @return This builder.
     */
    public Builder withLauncherDiscoveryListeners(List<String> value) {
      this.launcherDiscoveryListeners = value;
      return this;
    }

    /**
     * Configures the Jupiter Test Discovery Launcher to manually register the given test execution
     * listeners, specified by fully-qualified class name. Each class must declare a public
     * zero-argument constructor.
     *
     * @param value list of fully-qualified class names
     * @return This builder.
     */
    public Builder withTestExecutionListeners(List<String> value) {
      this.testExecutionListeners = value;
      return this;
    }

    /**
     * Configures the Jupiter Test Discovery Launcher to manually register the given post discovery
     * filters, specified by fully-qualified class name. Each class must declare a public
     * zero-argument constructor.
     *
     * @param value list of fully-qualified class names
     * @return This builder.
     */
    public Builder withPostDiscoveryFilters(List<String> value) {
      this.postDiscoveryFilters = value;
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
    this.testEngineAutoRegistrationEnabled = builder.testEngineAutoRegistrationEnabled;
    this.launcherSessionListenerAutoRegistrationEnabled =
        builder.launcherSessionListenerAutoRegistrationEnabled;
    this.launcherDiscoveryListenerAutoRegistrationEnabled =
        builder.launcherDiscoveryListenerAutoRegistrationEnabled;
    this.testExecutionListenerAutoRegistrationEnabled =
        builder.testExecutionListenerAutoRegistrationEnabled;
    this.postDiscoveryFilterAutoRegistrationEnabled =
        builder.postDiscoveryFilterAutoRegistrationEnabled;
    this.testEngines = builder.testEngines;
    this.launcherSessionListeners = builder.launcherSessionListeners;
    this.launcherDiscoveryListeners = builder.launcherDiscoveryListeners;
    this.testExecutionListeners = builder.testExecutionListeners;
    this.postDiscoveryFilters = builder.postDiscoveryFilters;
  }

  /**
   * Executes a JUnit Jupiter test discovery and collects the result.
   *
   * @return The result of discovered tests.
   */
  private Result collectTests0() {

    Set<Path> classPathRoots = new HashSet<>();
    classPathRoots.add(Paths.get(classDirectory.getAbsolutePath()));

    LauncherDiscoveryRequest request =
        LauncherDiscoveryRequestBuilder.request()
            .selectors(selectClasspathRoots(classPathRoots))
            .selectors(selectDirectory(classDirectory))
            .build();

    LauncherConfig config =
        LauncherConfig.builder()
            .enableTestEngineAutoRegistration(testEngineAutoRegistrationEnabled)
            .enableLauncherSessionListenerAutoRegistration(
                launcherSessionListenerAutoRegistrationEnabled)
            .enableLauncherDiscoveryListenerAutoRegistration(
                launcherDiscoveryListenerAutoRegistrationEnabled)
            .enableTestExecutionListenerAutoRegistration(
                testExecutionListenerAutoRegistrationEnabled)
            .enablePostDiscoveryFilterAutoRegistration(postDiscoveryFilterAutoRegistrationEnabled)
            .addTestEngines(instantiateAll(testEngines, TestEngine.class, TestEngine[]::new))
            .addLauncherSessionListeners(
                instantiateAll(
                    launcherSessionListeners,
                    LauncherSessionListener.class,
                    LauncherSessionListener[]::new))
            .addLauncherDiscoveryListeners(
                instantiateAll(
                    launcherDiscoveryListeners,
                    LauncherDiscoveryListener.class,
                    LauncherDiscoveryListener[]::new))
            .addTestExecutionListeners(
                instantiateAll(
                    testExecutionListeners,
                    TestExecutionListener.class,
                    TestExecutionListener[]::new))
            .addPostDiscoveryFilters(
                instantiateAll(
                    postDiscoveryFilters,
                    PostDiscoveryFilter.class,
                    PostDiscoveryFilter[]::new))
            .build();

    TestPlan testPlan = LauncherFactory.create(config).discover(request);

    Result result = new Result();

    for (TestIdentifier rootIdentifier : testPlan.getRoots()) {

      for (TestIdentifier identifier : testPlan.getChildren(rootIdentifier)) {

        fullyQualifiedName(identifier)
            .ifPresent(
                fqn -> {
                  Selector selector = new SuiteSelector();

                  Item item = new Item();
                  item.fullyQualifiedClassName = fqn;
                  item.selectors.add(selector);
                  item.explicit = false;

                  result.discoveredTests.add(item);
                });
      }
    }

    return result;
  }

  private <T> T[] instantiateAll(
      List<String> fqns, Class<T> type, IntFunction<T[]> arrayFactory) {

    T[] instances = arrayFactory.apply(fqns.size());
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    for (int i = 0; i < fqns.size(); i++) {
      String fqn = fqns.get(i);
      try {
        Class<?> cls = Class.forName(fqn, true, loader);
        Object instance = cls.getConstructor().newInstance();
        if (!type.isInstance(instance)) {
          throw new IllegalArgumentException(
              "'" + fqn + "' does not implement " + type.getName());
        }
        instances[i] = type.cast(instance);
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException("Failed to instantiate '" + fqn + "'", e);
      }
    }
    return instances;
  }

  /**
   * @return Optional.empty if the test is not a class or method
   */
  private Optional<String> fullyQualifiedName(TestIdentifier testIdentifier) {

    TestSource testSource = testIdentifier.getSource().orElse(null);

    if (testSource instanceof ClassSource) {

      ClassSource classSource = (ClassSource) testSource;
      return Optional.of(classSource.getClassName());
    }

    if (testSource instanceof MethodSource) {

      MethodSource methodSource = (MethodSource) testSource;
      return Optional.of(
          methodSource.getClassName()
              + '#'
              + methodSource.getMethodName()
              + '('
              + methodSource.getMethodParameterTypes()
              + ')');
    }

    return Optional.empty();
  }

  /**
   * Replaces the current threads context classloader before executing the specified callable.
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
    } finally {
      Thread.currentThread().setContextClassLoader(originalClassLoader);
    }
  }
}
