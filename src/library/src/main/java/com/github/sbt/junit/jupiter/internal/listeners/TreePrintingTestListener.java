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

import com.github.sbt.junit.jupiter.api.JupiterTestListener;
import com.github.sbt.junit.jupiter.internal.ColorTheme;
import com.github.sbt.junit.jupiter.internal.Configuration;
import com.github.sbt.junit.jupiter.internal.TestLogger;
import java.util.Objects;
import java.util.Optional;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * @author Michael Aichler
 */
public class TreePrintingTestListener implements JupiterTestListener {

  private TestPlan testPlan;
  private final TestLogger logger;
  private final ColorTheme colorTheme;
  private final Configuration configuration;
  private String indent = "";

  public TreePrintingTestListener(Configuration configuration) {

    this.colorTheme = configuration.getColorTheme();
    this.configuration = configuration;
    this.logger = configuration.getLogger();
  }

  @Override
  public void executionFailed(String className, Throwable throwable) {

    String message = "Execution of test " + className + " failed: " + throwable.getMessage();
    logger.error(className, message, throwable);
  }

  @Override
  public void executionFiltered(TestDescriptor descriptor, String reason) {}

  @Override
  public void testPlanExecutionStarted(TestPlan testPlan) {
    this.testPlan = testPlan;
  }

  @Override
  public void dynamicTestRegistered(TestIdentifier testIdentifier) {}

  @Override
  public void executionSkipped(TestIdentifier testIdentifier, String reason) {

    maybeIncreaseIndent(testIdentifier);

    String fqn = colorTheme.info().format(testIdentifier.getDisplayName());
    String prefix = testIdentifier.isTest() ? colorTheme.ignoreCount().format("o ") : "";

    log(prefix + fqn);
  }

  @Override
  public void executionStarted(TestIdentifier testIdentifier) {

    maybeIncreaseIndent(testIdentifier);

    String fqn = colorTheme.info().format(testIdentifier.getDisplayName());
    String prefix = testIdentifier.isTest() ? colorTheme.successful().format("+ ") : "";

    log(prefix + fqn);
  }

  @Override
  public void executionFinished(TestIdentifier identifier, TestExecutionResult result) {

    Throwable throwable = result.getThrowable().orElse(null);
    String fqn, message;

    switch (result.getStatus()) {
      case ABORTED:
        fqn = configuration.buildErrorName(identifier);
        message = configuration.buildErrorMessage(throwable);
        message = "Test assumption in test " + fqn + " failed: " + message;
        logger.warn(message);
        break;
      case FAILED:
        fqn = configuration.buildErrorName(identifier);
        message = configuration.buildErrorMessage(throwable);
        message = "Test " + fqn + " failed: " + message;
        logger.error(configuration.extractClassName(identifier), message, throwable);
        break;
      case SUCCESSFUL:
        fqn = identifier.getLegacyReportingName();
        message = "Test " + fqn + " finished";
        logger.debug(message);
        break;
    }

    maybeDecreaseIndent(identifier);
  }

  @Override
  public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {}

  /**
   * Increases current indent if the specified identifier has a parent container.
   *
   * @param identifier The test identifier.
   */
  private void maybeIncreaseIndent(TestIdentifier identifier) {

    if (hasParentContainer(identifier)) {
      indent += "  ";
    }
  }

  /**
   * Decreases current indent if the specified identifier has a parent container.
   *
   * @param identifier The test identifier.
   */
  private void maybeDecreaseIndent(TestIdentifier identifier) {

    if (hasParentContainer(identifier)) {
      indent = indent.length() > 1 ? indent.substring(2) : "";
    }
  }

  /**
   * @param identifier The test identifier to check.
   * @return True, if the specified identifier has a parent container.
   */
  private boolean hasParentContainer(TestIdentifier identifier) {

    return Optional.of(identifier)
        .flatMap(testPlan::getParent)
        .filter(Objects::nonNull)
        .filter(TestIdentifier::isContainer)
        .map(t -> true)
        .orElse(false);
  }

  private void log(String message) {

    logger.info(indent + message);
  }
}
