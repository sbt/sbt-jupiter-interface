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

import com.github.sbt.junit.jupiter.api.JupiterTestListener;
import com.github.sbt.junit.jupiter.internal.listeners.FlatPrintingTestListener;
import com.github.sbt.junit.jupiter.internal.listeners.TreePrintingTestListener;
import com.github.sbt.junit.jupiter.internal.options.Options;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.UniqueId.Segment;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import sbt.testing.Logger;
import sbt.testing.TaskDef;

/** @author Michael Aichler */
@SuppressWarnings("WeakerAccess")
public class Configuration {

  private final Options options;
  private final TestLogger logger;
  private final ColorTheme colorTheme = new ColorTheme() {};
  private final String testSuiteName;

  public Configuration(String testSuiteName, Logger[] loggers, Options options) {

    this.options = options;
    this.testSuiteName = testSuiteName;
    this.logger = new TestLogger(loggers, this);
  }

  /** @return The configured color theme. */
  public ColorTheme getColorTheme() {

    return colorTheme;
  }

  /** @return The configured test logger. */
  public TestLogger getLogger() {

    return logger;
  }

  /** @return The provided commandline options. */
  public Options getOptions() {

    return options;
  }

  /**
   * @see TaskDef#fullyQualifiedName()
   * @return The name of the test suite which is currently executed.
   */
  public String getTestSuiteName() {

    return testSuiteName;
  }

  /** @return The configured test listener. */
  public JupiterTestListener getTestListener() {

    switch (options.getDisplayMode()) {
      case "tree":
        return new TreePrintingTestListener(this);
      default:
        return new FlatPrintingTestListener(this);
    }
  }

  /**
   * Creates instances of test listeners using the specified {@code classLoader}.
   *
   * @param classLoader The class loader which should be used to load test listeners.
   * @return The list of test listener instances supplied via the command line.
   */
  public Optional<TestExecutionListener> createRunListener(ClassLoader classLoader) {

    return options
        .getRunListener()
        .map(
            className -> {
              try {
                final Object listener = classLoader.loadClass(className).newInstance();
                return (TestExecutionListener) listener;
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
  }

  /**
   * @param name The name which is to be decoded.
   * @return The decoded name, if {@link Options#isDecodeScalaNames()} evaluates to true.
   */
  public String decodeName(String name) {

    if (!options.isDecodeScalaNames()) {
      return name;
    }

    try {
      Class<?> cl = Class.forName("scala.reflect.NameTransformer");
      Method m = cl.getMethod("decode", String.class);
      String decoded = (String) m.invoke(null, name);
      return decoded == null ? name : decoded;
    } catch (Throwable t) {
      return name;
    }
  }

  /**
   * @param testPlan The test plan of the specified identifier.
   * @param identifier The test identifier which is to be formatted.
   * @return The formatted display name for the specified identifier.
   */
  public String formatIdentifier(TestPlan testPlan, TestIdentifier identifier) {

    return new TestIdentifierFormatter(testPlan, identifier).format();
  }

  /**
   * Extracts the class-name or alternatively the display name from the given identifier.
   *
   * @param identifier The identifier from which to extract the class-name or display name.
   * @return The class-name of the specified test identifier if a source is attached to the
   *     identifier, otherwise the display name is returned.
   */
  public String extractClassNameOrDisplayName(TestIdentifier identifier) {
    return identifier
        .getSource()
        .map(s -> extractClassName(identifier))
        .orElse(identifier.getDisplayName());
  }

  /**
   * Extracts the class-name from the specified test identifier.
   *
   * @param identifier The identifier from which to extract the class-name.
   * @return The class-name of the specified test identifier.
   */
  public String extractClassName(TestIdentifier identifier) {

    TestSource testSource =
        identifier
            .getSource()
            .orElseThrow(
                () -> new RuntimeException("Test identifier without source: " + identifier));

    if (testSource instanceof ClassSource) {
      return ((ClassSource) testSource).getClassName();
    }

    if (testSource instanceof MethodSource) {
      return ((MethodSource) testSource).getClassName();
    }

    throw new RuntimeException("Test identifier with unknown source: " + identifier);
  }

  /**
   * Extracts the method-name from the specified test identifier.
   *
   * @param identifier The identifier from which to extract the method-name.
   * @return The method-name of the specified test identifier.
   */
  public Optional<String> extractMethodName(TestIdentifier identifier) {

    TestSource testSource = identifier.getSource().orElse(null);

    if (testSource instanceof MethodSource) {

      MethodSource methodSource = (MethodSource) testSource;
      return Optional.of(methodSource.getMethodName());
    }

    return Optional.empty();
  }

  public String buildInfoMessage(Throwable t) {
    return buildColoredMessage(t, colorTheme.normalName2());
  }

  public String buildInfoName(TestIdentifier identifier) {
    return buildColoredName(
        identifier, colorTheme.normalName1(), colorTheme.normalName2(), colorTheme.normalName3());
  }

  public String buildErrorMessage(Throwable t) {

    return buildColoredMessage(t, colorTheme.errorName2());
  }

  public String buildErrorName(TestIdentifier identifier) {
    return buildColoredName(
        identifier, colorTheme.errorName1(), colorTheme.errorName2(), colorTheme.errorName3());
  }

  private String buildColoredName(TestIdentifier identifier, Color c1, Color c2, Color c3) {

    String className = extractClassNameOrDisplayName(identifier);
    Optional<String> methodName = extractMethodName(identifier);

    StringBuilder b = new StringBuilder();
    b.append(buildColoredClassName(decodeName(className), c1));

    methodName.ifPresent(m -> b.append(buildColoredMethodName(m, c2, c3)));
    return b.toString();
  }

  private String buildColoredMessage(Throwable t, Color color) {

    if (t == null) {
      return "null";
    }

    if (!options.isExceptionClassLogEnabled()) {
      return t.getMessage();
    }

    if (!options.isAssertLogEnabled()) {
      if (t instanceof AssertionError) {
        return t.getMessage();
      }
    }

    String className = decodeName(t.getClass().getName());
    return buildColoredClassName(className, color) + ": " + t.getMessage();
  }

  private String buildColoredClassName(String className, Color color) {

    int nestedClassPos = className.indexOf('$');
    int simpleNamePos =
        nestedClassPos == -1
            ? className.lastIndexOf('.')
            : className.lastIndexOf('.', nestedClassPos);

    if (simpleNamePos == -1) {
      return color.format(className);
    }

    String packagePrefix = className.substring(0, simpleNamePos);
    String simpleName = className.substring(simpleNamePos + 1);

    return packagePrefix + '.' + color.format(simpleName);
  }

  private String buildColoredMethodName(String m, Color c1, Color c2) {

    StringBuilder b = new StringBuilder();
    b.append('.');
    int mpos1 = m.lastIndexOf('[');
    int mpos2 = m.lastIndexOf(']');
    if (mpos1 == -1 || mpos2 < mpos1) {

      b.append(c1.format(decodeName(m)));
    } else {
      b.append(c1.format(decodeName(m.substring(0, mpos1))));
      b.append('[');
      b.append(c2.format(m.substring(mpos1 + 1, mpos2)));
      b.append(']');
    }

    return b.toString();
  }

  /** Helper class which knows how to format a {@link TestIdentifier}. */
  class TestIdentifierFormatter {

    static final String VINTAGE_ENGINE = "junit-vintage";

    final TestPlan testPlan;
    final TestIdentifier identifier;

    private String testEngine;

    TestIdentifierFormatter(TestPlan testPlan, TestIdentifier testIdentifier) {

      this.testPlan = testPlan;
      this.identifier = testIdentifier;
    }

    /** @return The formatted test name using the configured color theme. */
    public String format() {

      final List<TestIdentifier> path = getPath(testPlan, identifier);

      testEngine = UniqueId.parse(identifier.getUniqueId()).getEngineId().orElse(null);

      return path.stream()
          .skip(1)
          .map(this::toName)
          .filter(Objects::nonNull)
          .collect(Collectors.joining());
    }

    private List<TestIdentifier> getPath(TestPlan testPlan, TestIdentifier identifier) {

      List<TestIdentifier> result = new ArrayList<>();

      do {
        if (identifier.getSource().isPresent()) {
          result.add(identifier);
        }
        identifier = testPlan.getParent(identifier).orElse(null);
      } while (null != identifier);

      Collections.reverse(result);
      return result;
    }

    /**
     * @return A formatted display name on success, {@code NULL} if the given identifier should be
     *     ignored.
     */
    private String toName(TestIdentifier identifier) {

      String name = identifier.getDisplayName();
      List<Segment> segments = UniqueId.parse(identifier.getUniqueId()).getSegments();

      if (!segments.isEmpty()) {
        Segment lastSegment = segments.get(segments.size() - 1);

        name =
            VINTAGE_ENGINE.equals(testEngine)
                ? toVintageName(identifier, lastSegment)
                : toName(lastSegment);
      }

      return name;
    }

    /*
     * Formats a test segment from junit-jupiter.
     */
    private String toName(Segment segment) {

      String name;

      switch (segment.getType()) {
        case "class":
          name = colorClassName(segment.getValue(), colorTheme.container());
          break;
        case "nested-class":
          name = colorTheme.container().format("$" + segment.getValue());
          break;
        case "method":
          name = colorTheme.testMethod().format("#" + segment.getValue());
          break;
        case "test-factory":
          name = colorTheme.testFactory().format("#" + segment.getValue());
          break;
        case "dynamic-test":
          name = colorTheme.dynamicTest().format(":" + segment.getValue());
          break;
        case "test-template":
          name = colorTheme.testTemplate().format("#" + segment.getValue());
          break;
        case "test-template-invocation":
          name = colorTheme.container().format(":" + segment.getValue());
          break;
        default:
          name = segment.getValue();
          break;
      }

      if (options.isTypesEnabled()) {
        name = segment.getType() + ":" + name;
      }

      return name;
    }

    /*
     * Formats a test identifier run by junit-vintage engine.
     */
    private String toVintageName(TestIdentifier identifier, Segment lastSegment) {

      final String type = lastSegment.getType();

      if ("runner".equals(type)) {

        String className = identifier.getLegacyReportingName();
        return colorClassName(className, colorTheme.container());
      }

      if ("test".equals(type)) {

        final TestSource source = identifier.getSource().orElse(null);

        if (null == source) {
          // Caused by Parameterized test runner, display name usually is the index name in
          // brackets.
          // Ignored since the index name is repeated in the display name of the test method.
          return null;
        }

        if (source instanceof ClassSource) {
          String nestedClassName = "$" + identifier.getDisplayName().replaceFirst(".*?\\$", "");
          return colorTheme.container().format(nestedClassName);
        }

        if (source instanceof MethodSource) {
          String testName = "#" + identifier.getDisplayName();
          return colorTheme.testMethod().format(testName);
        }
      }

      return "/" + identifier.getDisplayName();
    }

    /*
     * Colors the last part of <className> with the given <color>.
     */
    private String colorClassName(String className, Color color) {

      String[] parts = className.split("\\.");
      return IntStream.range(0, parts.length)
          .mapToObj(
              i -> {
                if (i == (parts.length - 1)) return color.format(parts[i]);
                return parts[i];
              })
          .collect(Collectors.joining("."));
    }
  }
}
