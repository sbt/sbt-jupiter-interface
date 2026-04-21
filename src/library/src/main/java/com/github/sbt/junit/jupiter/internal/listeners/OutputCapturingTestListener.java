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

import static org.junit.platform.engine.TestExecutionResult.Status.SUCCESSFUL;

import com.github.sbt.junit.jupiter.api.StreamPair;
import com.github.sbt.junit.jupiter.internal.Configuration;
import com.github.sbt.junit.jupiter.internal.TestLogger;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * @author Michael Aichler
 */
public class OutputCapturingTestListener implements TestExecutionListener {

  private final Map<String, CapturedOutputStream> outputStreamMap = new ConcurrentHashMap<>();
  private final Consumer<String> outputConsumer;
  private final Consumer<String> errorConsumer;
  private final StreamPair systemStreamPair;
  private final TestLogger testLogger;
  private final boolean isQuiet;

  public OutputCapturingTestListener(Configuration configuration, StreamPair systemStreamPair) {

    this.isQuiet = configuration.getOptions().isQuiet();
    this.testLogger = configuration.getLogger();
    this.outputConsumer = isQuiet ? testLogger::debug : testLogger::info;
    this.errorConsumer = testLogger::error;
    this.systemStreamPair = systemStreamPair;
  }

  @Override
  public void testPlanExecutionStarted(TestPlan testPlan) {

    OutputCapture.install(systemStreamPair);
  }

  @Override
  public void testPlanExecutionFinished(TestPlan testPlan) {

    OutputCapture.uninstall();
  }

  @Override
  public void executionStarted(TestIdentifier identifier) {

    outputStreamMap.computeIfAbsent(
        identifier.getUniqueId(),
        key -> {
          final CapturedOutputStream outputStream = new CapturedOutputStream(outputConsumer);
          final CapturedOutputStream errorStream = new CapturedOutputStream(errorConsumer);

          OutputCapture.register(
              new PrintStream(outputStream, true), new PrintStream(errorStream, true));

          return outputStream;
        });
  }

  @Override
  public void executionFinished(TestIdentifier identifier, TestExecutionResult result) {

    CapturedOutputStream outputStream = outputStreamMap.remove(identifier.getUniqueId());
    if (null == outputStream) {
      return;
    }

    OutputCapture.deregister();

    if (isQuiet) {
      if (identifier.isTest()) {
        if (!SUCCESSFUL.equals(result.getStatus())) {
          outputStream.output.forEach(testLogger::info);
          outputStream.output.clear();
        }
      }
    }
  }

  static class CapturedOutputStream extends ByteArrayOutputStream {

    final Consumer<String> consumer;
    final List<String> output = new ArrayList<>();

    CapturedOutputStream(Consumer<String> consumer) {

      this.consumer = consumer;
    }

    @Override
    public void flush() throws IOException {

      final String[] lines = toString().split("\\r?\\n");
      for (String line : lines) {
        if (!line.isEmpty()) {
          consumer.accept(line);
          output.add(line);
        }
      }
      reset();
    }
  }
}
