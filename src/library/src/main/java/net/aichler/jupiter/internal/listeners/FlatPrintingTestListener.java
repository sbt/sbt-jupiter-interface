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
package net.aichler.jupiter.internal.listeners;

import net.aichler.jupiter.api.JupiterTestListener;
import net.aichler.jupiter.internal.ColorTheme;
import net.aichler.jupiter.internal.Configuration;
import net.aichler.jupiter.internal.TestLogger;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Michael Aichler
 */
public class FlatPrintingTestListener implements JupiterTestListener {

    private TestPlan testPlan;
    private final TestLogger logger;
    private final ColorTheme colorTheme;
    private final Configuration configuration;
    private final Map<String,Long> startTimes = new ConcurrentHashMap<>();

    public FlatPrintingTestListener(Configuration configuration) {

        this.colorTheme = configuration.getColorTheme();
        this.configuration = configuration;
        this.logger = configuration.getLogger();
    }


    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {

        this.testPlan = testPlan;
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {

        String fqn = configuration.formatIdentifier(testPlan, testIdentifier);
        String message = "Test " + fqn + " ignored" + Optional.ofNullable(reason)
                .map(s -> ": " + s).orElse("");

        logger.info(message);
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {

        startTimes.putIfAbsent(testIdentifier.getUniqueId(), System.currentTimeMillis());

        if (!testIdentifier.getParentId().isPresent()) {
            if (!testPlan.getChildren(testIdentifier).isEmpty()) {
                String message = "Test run started (" + testIdentifier.getDisplayName() + ")";
                debugOrInfo(colorTheme.info().format(message));
            }
        }

        if (testIdentifier.isTest()) {

            String testName = configuration.formatIdentifier(testPlan, testIdentifier);
            debugOrInfo("Test " + testName + " started");
        }
    }

    @Override
    public void executionFinished(TestIdentifier identifier, TestExecutionResult result) {

        String duration = calculateDurationSuffix(identifier.getUniqueId());
        Throwable throwable = result.getThrowable().orElse(null);
        String fqn, message;

        switch (result.getStatus()) {
            case ABORTED:
                fqn = configuration.buildErrorName(identifier);
                message = configuration.buildErrorMessage(throwable);
                message = "Test assumption in test " + fqn + " failed: " + message + duration;
                logger.warn(message);
                break;
            case FAILED:
                fqn = configuration.buildErrorName(identifier);
                message = configuration.buildErrorMessage(throwable);
                message = "Test " + fqn + " failed: " + message + duration;
                logger.error(configuration.extractClassNameOrDisplayName(identifier), message, throwable);
                break;
            case SUCCESSFUL:
                fqn = configuration.formatIdentifier(testPlan, identifier);
                message = "Test " + fqn + " finished" + duration;
                logger.debug(message);
                break;
        }
    }

    @Override
    public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {

    }

    private long calculateDuration(String uniqueId) {

        final long startTime = startTimes.getOrDefault(uniqueId, 0L);
        return (0L == startTime ? startTime : System.currentTimeMillis() - startTime);
    }

    private String calculateDurationSuffix(String uniqueId) {

        long duration = calculateDuration(uniqueId);
        return  ", took " + (duration / 1000.0) + "s";
    }

    private void debugOrInfo(String message) {

        if (configuration.getOptions().isVerbose()) {
            logger.info(message);
            return;
        }

        logger.debug(message);
    }
}
