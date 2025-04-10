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
package com.github.sbt.junit.jupiter.internal;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.launcher.TagFilter.excludeTags;
import static org.junit.platform.launcher.TagFilter.includeTags;

import com.github.sbt.junit.jupiter.api.JupiterTestListener;
import com.github.sbt.junit.jupiter.api.StreamPair;
import com.github.sbt.junit.jupiter.internal.event.Dispatcher;
import com.github.sbt.junit.jupiter.internal.event.LoggingEventHandler;
import com.github.sbt.junit.jupiter.internal.filter.GlobFilter;
import com.github.sbt.junit.jupiter.internal.filter.TestFilter;
import com.github.sbt.junit.jupiter.internal.listeners.OutputCapturingTestListener;
import com.github.sbt.junit.jupiter.internal.listeners.SummaryPrintingTestListener;
import com.github.sbt.junit.jupiter.internal.options.Options;
import com.github.sbt.junit.jupiter.internal.options.OptionsParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.Filter;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import sbt.testing.EventHandler;
import sbt.testing.Logger;
import sbt.testing.Runner;
import sbt.testing.Task;
import sbt.testing.TaskDef;

/**
 * @author Michael Aichler
 */
public class JupiterRunner implements Runner {

  private final StreamPair systemStreamPair;
  private final ClassLoader testClassLoader;
  private final String[] args;
  private final String[] remoteArgs;
  private final Options options;

  public JupiterRunner(
      String[] args,
      String[] remoteArgs,
      ClassLoader testClassLoader,
      StreamPair systemStreamPair) {

    this.args = args;
    this.remoteArgs = remoteArgs;
    this.testClassLoader = testClassLoader;
    this.systemStreamPair = systemStreamPair;
    this.options = new OptionsParser().parse(args);
  }

  @Override
  public String[] args() {

    return args;
  }

  @Override
  public String done() {

    return "";
  }

  @Override
  public String[] remoteArgs() {

    return remoteArgs;
  }

  @Override
  public Task[] tasks(TaskDef[] taskDefs) {

    return Arrays.stream(taskDefs).map(JupiterTask::new).toArray(Task[]::new);
  }

  /**
   * Jupiter test task.
   *
   * @author Michael Aichler
   */
  class JupiterTask implements Task {

    final TaskDef taskDef;

    JupiterTask(TaskDef taskDef) {

      this.taskDef = taskDef;
    }

    @Override
    public String[] tags() {

      return new String[0];
    }

    @Override
    public Task[] execute(EventHandler eventHandler, Logger[] loggers) {

      return new WithCustomProperties(new JupiterTaskExecutor(loggers, eventHandler, taskDef))
          .execute();
    }

    @Override
    public TaskDef taskDef() {

      return taskDef;
    }
  }

  /**
   * Jupiter test task executor.
   *
   * @author Michael Aichler
   */
  class JupiterTaskExecutor {

    final Logger[] loggers;
    final EventHandler eventHandler;
    final TaskDef taskDef;

    JupiterTaskExecutor(Logger[] loggers, EventHandler eventHandler, TaskDef taskDef) {

      this.loggers = loggers;
      this.taskDef = taskDef;
      this.eventHandler =
          options
              .getDispatchEventsTracePath()
              .map(path -> (EventHandler) new LoggingEventHandler(path, eventHandler))
              .orElse(eventHandler);
    }

    Task[] execute() {

      String testSuiteName = taskDef.fullyQualifiedName();

      Configuration configuration = new Configuration(testSuiteName, loggers, options);
      Dispatcher dispatcher = new Dispatcher(configuration, eventHandler);

      SummaryPrintingTestListener summaryListener = new SummaryPrintingTestListener(configuration);
      TestExecutionListener outputCapturingListener =
          new OutputCapturingTestListener(configuration, systemStreamPair);

      JupiterTestListener testListener = configuration.getTestListener();

      try {
        LauncherDiscoveryRequestBuilder builder = LauncherDiscoveryRequestBuilder.request();
        builder.selectors(testSelector(testSuiteName));
        builder.filters(testFilters(dispatcher));

        Launcher launcher = LauncherFactory.create();

        launcher.registerTestExecutionListeners(dispatcher);
        launcher.registerTestExecutionListeners(outputCapturingListener);
        launcher.registerTestExecutionListeners(summaryListener);
        launcher.registerTestExecutionListeners(testListener);

        configuration
            .createRunListener(testClassLoader)
            .ifPresent(launcher::registerTestExecutionListeners);

        launcher.execute(builder.build(), new TestExecutionListener[0]);

        return new Task[0];
      } catch (Throwable t) {
        dispatcher.executionFailed(testSuiteName, t);
        t.printStackTrace();
        return new Task[0];
      }
    }

    private DiscoverySelector testSelector(String testClassName) {

      if (testClassName.contains("#")) {
        return selectMethod(testClassName);
      }

      return selectClass(testClassName);
    }

    private Filter[] testFilters(Dispatcher dispatcher) {

      List<Filter<?>> filters = new ArrayList<>();

      if (!options.getExcludeTags().isEmpty()) {
        filters.add(excludeTags(options.getExcludeTags()));
      }

      if (!options.getIncludeTags().isEmpty()) {
        filters.add(includeTags(options.getIncludeTags()));
      }

      TestFilter.create(options.getTestFilters(), dispatcher).ifPresent(filters::add);
      GlobFilter.create(options.getGlobPatterns(), dispatcher).ifPresent(filters::add);

      return filters.toArray(new Filter[filters.size()]);
    }
  }

  class WithCustomProperties {

    private final JupiterTaskExecutor wrapped;
    private final Map<String, String> tempProperties = new HashMap<>();

    WithCustomProperties(JupiterTaskExecutor wrapped) {

      this.wrapped = wrapped;
    }

    Task[] execute() {

      try {
        createTempProperties(options.getSystemProperties());
        return wrapped.execute();
      } finally {
        restoreSystemProperties();
      }
    }

    private void createTempProperties(Map<String, String> systemProperties) {

      synchronized (System.getProperties()) {
        for (Map.Entry<String, String> entry : systemProperties.entrySet()) {
          tempProperties.put(entry.getKey(), entry.getValue());
          System.setProperty(entry.getKey(), entry.getValue());
        }
      }
    }

    private void restoreSystemProperties() {

      synchronized (System.getProperties()) {
        tempProperties.forEach(
            (key, value) -> {
              if (null == value) {
                System.clearProperty(key);
              } else {
                System.setProperty(key, value);
              }
            });
        tempProperties.clear();
      }
    }
  }
}
