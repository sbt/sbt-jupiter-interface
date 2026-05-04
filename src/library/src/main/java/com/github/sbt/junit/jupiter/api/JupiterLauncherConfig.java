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

/**
 * Configures the Jupiter Test Discovery Launcher.
 *
 * <p>Each component toggles automatic registration of one of JUnit Platform's discoverable service
 * provider interfaces. The corresponding implementations need to be present on the classpath to be
 * registered automatically.
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
 */
public record JupiterLauncherConfig(
    boolean testEngineAutoRegistrationEnabled,
    boolean launcherSessionListenerAutoRegistrationEnabled,
    boolean launcherDiscoveryListenerAutoRegistrationEnabled,
    boolean testExecutionListenerAutoRegistrationEnabled,
    boolean postDiscoveryFilterAutoRegistrationEnabled) {

  /** A configuration with auto registration enabled for all service provider interfaces. */
  public static final JupiterLauncherConfig DEFAULT =
      new JupiterLauncherConfig(true, true, true, true, true);
}
