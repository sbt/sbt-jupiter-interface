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

import java.util.List;
import java.util.function.IntFunction;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.launcher.LauncherDiscoveryListener;
import org.junit.platform.launcher.LauncherSessionListener;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.core.LauncherConfig;

/**
 * Configures the Jupiter Test Discovery Launcher.
 *
 * <p>The first 5 components toggle automatic registration of one of JUnit Platform's discoverable
 * service provider interfaces. The corresponding implementations need to be present on the
 * classpath to be registered automatically.
 *
 * <p>The remaining 5 components let the caller register additional implementations explicitly by
 * fully qualified class name. This is typically used in combination with the matching {@code
 * *AutoRegistrationEnabled = false} flag, where the user wants to provide a hand-picked set of SPI
 * implementations rather than rely on classpath auto-discovery (or where the runner runs in a
 * separate JVM and instances cannot be transported across the JVM boundary). Each named class is
 * loaded via the test classloader and instantiated through its {@code public} no-arg constructor at
 * the moment a JUnit {@link LauncherConfig} is built. Classes that are missing, lack a {@code
 * public} no-arg constructor, or do not implement the corresponding SPI interface cause a {@link
 * RuntimeException} at that point.
 *
 * @param testEngineAutoRegistrationEnabled enable/disable auto registration of test engines. Test
 *     engines need to be present on the classpath to be registered automatically.
 * @param launcherSessionListenerAutoRegistrationEnabled enable/disable auto registration of
 *     launcher session listeners. Launcher session listeners need to be present on the classpath to
 *     be registered automatically.
 * @param launcherDiscoveryListenerAutoRegistrationEnabled enable/disable auto registration of
 *     launcher discovery listeners. Launcher discovery listeners need to be present on the
 *     classpath to be registered automatically.
 * @param testExecutionListenerAutoRegistrationEnabled enable/disable auto registration of test
 *     execution listeners. Test execution listeners need to be present on the classpath to be
 *     registered automatically.
 * @param postDiscoveryFilterAutoRegistrationEnabled enable/disable auto registration of post
 *     discovery filters. Post discovery filters need to be present on the classpath to be
 *     registered automatically.
 * @param testEngineClassNames fully qualified class names of additional {@link TestEngine}
 *     implementations to register with the launcher. Each class is instantiated via its {@code
 *     public} no-arg constructor.
 * @param launcherSessionListenerClassNames fully qualified class names of additional {@link
 *     LauncherSessionListener} implementations to register with the launcher. Each class is
 *     instantiated via its {@code public} no-arg constructor.
 * @param launcherDiscoveryListenerClassNames fully qualified class names of additional {@link
 *     LauncherDiscoveryListener} implementations to register with the launcher. Each class is
 *     instantiated via its {@code public} no-arg constructor.
 * @param testExecutionListenerClassNames fully qualified class names of additional {@link
 *     TestExecutionListener} implementations to register with the launcher. Each class is
 *     instantiated via its {@code public} no-arg constructor.
 * @param postDiscoveryFilterClassNames fully qualified class names of additional {@link
 *     PostDiscoveryFilter} implementations to register with the launcher. Each class is
 *     instantiated via its {@code public} no-arg constructor.
 */
public record JupiterLauncherConfig(
    boolean testEngineAutoRegistrationEnabled,
    boolean launcherSessionListenerAutoRegistrationEnabled,
    boolean launcherDiscoveryListenerAutoRegistrationEnabled,
    boolean testExecutionListenerAutoRegistrationEnabled,
    boolean postDiscoveryFilterAutoRegistrationEnabled,
    List<String> testEngineClassNames,
    List<String> launcherSessionListenerClassNames,
    List<String> launcherDiscoveryListenerClassNames,
    List<String> testExecutionListenerClassNames,
    List<String> postDiscoveryFilterClassNames) {

  /** A configuration with auto registration enabled for all service provider interfaces. */
  public static final JupiterLauncherConfig DEFAULT =
      new JupiterLauncherConfig(
          true, true, true, true, true, List.of(), List.of(), List.of(), List.of(), List.of());

  /**
   * Builds a JUnit Platform {@link LauncherConfig} from this configuration. Each FQN in the 5 list
   * components is loaded through the supplied {@code cl} and instantiated via its {@code public}
   * no-arg constructor.
   *
   * @param cl class loader used to resolve the FQNs.
   * @return a JUnit Platform launcher configuration.
   * @throws RuntimeException if any FQN cannot be resolved, lacks a {@code public} no-arg
   *     constructor, or does not implement the corresponding SPI interface.
   */
  public LauncherConfig toJUnitConfig(ClassLoader cl) {
    return LauncherConfig.builder()
        .enableTestEngineAutoRegistration(testEngineAutoRegistrationEnabled)
        .enableLauncherSessionListenerAutoRegistration(
            launcherSessionListenerAutoRegistrationEnabled)
        .enableLauncherDiscoveryListenerAutoRegistration(
            launcherDiscoveryListenerAutoRegistrationEnabled)
        .enableTestExecutionListenerAutoRegistration(testExecutionListenerAutoRegistrationEnabled)
        .enablePostDiscoveryFilterAutoRegistration(postDiscoveryFilterAutoRegistrationEnabled)
        .addTestEngines(newInstances(cl, testEngineClassNames, TestEngine[]::new))
        .addLauncherSessionListeners(
            newInstances(cl, launcherSessionListenerClassNames, LauncherSessionListener[]::new))
        .addLauncherDiscoveryListeners(
            newInstances(cl, launcherDiscoveryListenerClassNames, LauncherDiscoveryListener[]::new))
        .addTestExecutionListeners(
            newInstances(cl, testExecutionListenerClassNames, TestExecutionListener[]::new))
        .addPostDiscoveryFilters(
            newInstances(cl, postDiscoveryFilterClassNames, PostDiscoveryFilter[]::new))
        .build();
  }

  @SuppressWarnings("unchecked")
  private static <T> T[] newInstances(
      ClassLoader cl, List<String> fqns, IntFunction<T[]> arrayFactory) {
    final var result = arrayFactory.apply(fqns.size());
    final var iface = result.getClass().getComponentType();
    for (var i = 0; i < fqns.size(); i++) {
      final var fqn = fqns.get(i);
      try {
        result[i] = (T) Class.forName(fqn, true, cl).getConstructor().newInstance();
      } catch (ReflectiveOperationException | ArrayStoreException | ClassCastException e) {
        throw new RuntimeException("Failed to instantiate " + fqn + " as " + iface.getName(), e);
      }
    }
    return result;
  }
}
