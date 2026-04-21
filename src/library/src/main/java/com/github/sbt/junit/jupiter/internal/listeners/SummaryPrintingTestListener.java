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

import com.github.sbt.junit.jupiter.internal.Color;
import com.github.sbt.junit.jupiter.internal.ColorTheme;
import com.github.sbt.junit.jupiter.internal.Configuration;
import com.github.sbt.junit.jupiter.internal.TestLogger;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * @author Michael Aichler
 */
public class SummaryPrintingTestListener extends SummaryGeneratingListener {

  private final ColorTheme colorTheme;
  private final TestLogger logger;
  private final boolean isVerbose;

  public SummaryPrintingTestListener(Configuration configuration) {

    this.colorTheme = configuration.getColorTheme();
    this.isVerbose = configuration.getOptions().isVerbose();
    this.logger = configuration.getLogger();
  }

  @Override
  public void testPlanExecutionFinished(TestPlan testPlan) {

    TestExecutionSummary summary = getSummary();
    long testRunDuration = System.currentTimeMillis() - summary.getTimeStarted();

    long totalFailureCount = summary.getTotalFailureCount();
    long testsSkippedCount = summary.getTestsSkippedCount();
    long totalTestsFound = summary.getTestsFoundCount();

    Color ignoreColor = testsSkippedCount > 0 ? colorTheme.ignoreCount() : colorTheme.info();
    Color errorColor = totalFailureCount > 0 ? colorTheme.errorCount() : colorTheme.info();

    debugOrInfo(
        ""
            + colorTheme.info().format("Test run finished: ")
            + errorColor.format(totalFailureCount + " failed")
            + colorTheme.info().format(", ")
            + ignoreColor.format(testsSkippedCount + " ignored")
            + colorTheme.info().format(", ")
            + colorTheme.info().format(totalTestsFound + " total, ")
            + colorTheme.info().format(testRunDuration / 1000.0 + "s"));
  }

  private void debugOrInfo(String message) {

    if (isVerbose) {
      logger.info(message);
      return;
    }

    logger.debug(message);
  }
}
