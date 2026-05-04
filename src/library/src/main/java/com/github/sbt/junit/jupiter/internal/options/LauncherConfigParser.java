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
package com.github.sbt.junit.jupiter.internal.options;

import com.github.sbt.junit.jupiter.api.JupiterLauncherConfig;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Internal parser for the launcher-config flags injected by the sbt plugin to transport a {@link
 * JupiterLauncherConfig} value across the JVM boundary into the test runner. These flag names are
 * an implementation detail; the user-facing configuration path is the {@code
 * jupiterTestRunningLauncherConfig} setting key.
 *
 * <p>For each flag, the LAST occurrence wins. The plugin always injects all flags ahead of any
 * user-supplied {@code testOnly --} arguments, so a user-supplied flag overrides the setting-key
 * value for that single test run.
 */
public final class LauncherConfigParser {

  static final String OPT_TEST_ENGINE = "--test-engine-auto-registration-enabled=";
  static final String OPT_LAUNCHER_SESSION =
      "--launcher-session-listener-auto-registration-enabled=";
  static final String OPT_LAUNCHER_DISCOVERY =
      "--launcher-discovery-listener-auto-registration-enabled=";
  static final String OPT_TEST_EXECUTION = "--test-execution-listener-auto-registration-enabled=";
  static final String OPT_POST_DISCOVERY = "--post-discovery-filter-auto-registration-enabled=";

  static final String OPT_TEST_ENGINE_CLASSES = "--test-engine-class-names=";
  static final String OPT_LAUNCHER_SESSION_CLASSES = "--launcher-session-listener-class-names=";
  static final String OPT_LAUNCHER_DISCOVERY_CLASSES = "--launcher-discovery-listener-class-names=";
  static final String OPT_TEST_EXECUTION_CLASSES = "--test-execution-listener-class-names=";
  static final String OPT_POST_DISCOVERY_CLASSES = "--post-discovery-filter-class-names=";

  public static JupiterLauncherConfig parse(String[] args) {

    var testEngine = true;
    var launcherSession = true;
    var launcherDiscovery = true;
    var testExecution = true;
    var postDiscovery = true;

    var testEngineClasses = List.<String>of();
    var launcherSessionClasses = List.<String>of();
    var launcherDiscoveryClasses = List.<String>of();
    var testExecutionClasses = List.<String>of();
    var postDiscoveryClasses = List.<String>of();

    for (final var arg : args) {
      if (arg.startsWith(OPT_TEST_ENGINE)) {
        testEngine = Boolean.parseBoolean(arg.substring(OPT_TEST_ENGINE.length()));
      } else if (arg.startsWith(OPT_LAUNCHER_SESSION)) {
        launcherSession = Boolean.parseBoolean(arg.substring(OPT_LAUNCHER_SESSION.length()));
      } else if (arg.startsWith(OPT_LAUNCHER_DISCOVERY)) {
        launcherDiscovery = Boolean.parseBoolean(arg.substring(OPT_LAUNCHER_DISCOVERY.length()));
      } else if (arg.startsWith(OPT_TEST_EXECUTION)) {
        testExecution = Boolean.parseBoolean(arg.substring(OPT_TEST_EXECUTION.length()));
      } else if (arg.startsWith(OPT_POST_DISCOVERY)) {
        postDiscovery = Boolean.parseBoolean(arg.substring(OPT_POST_DISCOVERY.length()));
      } else if (arg.startsWith(OPT_TEST_ENGINE_CLASSES)) {
        testEngineClasses = parseList(arg.substring(OPT_TEST_ENGINE_CLASSES.length()));
      } else if (arg.startsWith(OPT_LAUNCHER_SESSION_CLASSES)) {
        launcherSessionClasses = parseList(arg.substring(OPT_LAUNCHER_SESSION_CLASSES.length()));
      } else if (arg.startsWith(OPT_LAUNCHER_DISCOVERY_CLASSES)) {
        launcherDiscoveryClasses =
            parseList(arg.substring(OPT_LAUNCHER_DISCOVERY_CLASSES.length()));
      } else if (arg.startsWith(OPT_TEST_EXECUTION_CLASSES)) {
        testExecutionClasses = parseList(arg.substring(OPT_TEST_EXECUTION_CLASSES.length()));
      } else if (arg.startsWith(OPT_POST_DISCOVERY_CLASSES)) {
        postDiscoveryClasses = parseList(arg.substring(OPT_POST_DISCOVERY_CLASSES.length()));
      }
    }

    return new JupiterLauncherConfig(
        testEngine,
        launcherSession,
        launcherDiscovery,
        testExecution,
        postDiscovery,
        testEngineClasses,
        launcherSessionClasses,
        launcherDiscoveryClasses,
        testExecutionClasses,
        postDiscoveryClasses);
  }

  private static List<String> parseList(String value) {
    if (value.isEmpty()) {
      return List.of();
    }
    return Arrays.stream(value.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toUnmodifiableList());
  }

  private LauncherConfigParser() {}
}
