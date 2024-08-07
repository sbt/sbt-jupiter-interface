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
package com.github.sbt.junit.jupiter.internal.event;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestIdentifier;

/** @author Michael Aichler */
class TaskName {

  private String fullyQualifiedName;
  private String nestedSuiteId;
  private String testName;
  private String invocation;

  /** @return The fully qualified name of the test suite. */
  String fullyQualifiedName() {

    return fullyQualifiedName;
  }

  /** @return An optional nested suite id (might be {@code null}). */
  String nestedSuiteId() {

    return nestedSuiteId;
  }

  /** @return An optional test name (might be {@code null}). */
  String testName() {

    return testName;
  }

  /** @return An optional invocation for repeated tests. */
  String invocation() {

    return invocation;
  }

  /**
   * Creates a task name for the specified {@code testName}.
   *
   * @param testSuite The name of the test suite.
   * @param testName The name of the current test.
   * @return A task name instance representing the given testName.
   */
  static TaskName of(String testSuite, String testName) {

    if (!testSuite.equals(testName)) {
      throw new RuntimeException(testSuite + " != " + testName);
    }

    TaskName result = new TaskName();
    result.fullyQualifiedName = testName;
    return result;
  }

  /**
   * Creates a task name for the specified {@code identifier}.
   *
   * @param testSuite The name of the test suite.
   * @param identifier The test identifier.
   * @return A task name representing the given identifier.
   */
  static TaskName of(String testSuite, TestIdentifier identifier) {

    TaskName result = new TaskName();
    result.fullyQualifiedName = testSuite;

    TestSource testSource = identifier.getSource().orElse(null);

    if (testSource instanceof ClassSource) {

      ClassSource classSource = (ClassSource) testSource;
      result.nestedSuiteId = nestedSuiteId(testSuite, classSource.getClassName());
    }

    if (testSource instanceof MethodSource) {

      MethodSource methodSource = (MethodSource) testSource;
      result.nestedSuiteId = nestedSuiteId(testSuite, methodSource.getClassName());
      result.invocation = invocation(identifier, UniqueId.parse(identifier.getUniqueId()));
      result.testName =
          testName(methodSource.getMethodName(), methodSource.getMethodParameterTypes());
    }

    return result;
  }

  /**
   * Extracts a nested test suite identifier if available.
   *
   * @param testSuite The name of the enclosing test suite.
   * @param className The name of the current test class.
   * @return The nested suite identifier (might be {@code null}).
   */
  static String nestedSuiteId(String testSuite, String className) {

    if (!className.startsWith(testSuite)) {
      throw new RuntimeException("Test class " + className + " is not enclosed by " + testSuite);
    }

    if (!className.equals(testSuite)) {
      return className.substring(testSuite.length());
    }

    return null;
  }

  /**
   * Creates a test method name with simplified parameter types.
   *
   * @param methodName The name of the test method as reported by JUnit.
   * @param methodParameterTypes The method parameter types as reported by JUnit.
   * @return The test method signature with simplified parameter types.
   */
  static String testName(String methodName, String methodParameterTypes) {

    String parameterTypes = "";

    if (null != methodParameterTypes && !methodParameterTypes.isEmpty()) {
      String[] parts = methodParameterTypes.split(",\\s*");
      parameterTypes =
          Arrays.stream(parts)
              .map(
                  type -> {
                    int indexOf = type.lastIndexOf('.');
                    return (indexOf < 0) ? type : type.substring(indexOf + 1);
                  })
              .collect(Collectors.joining(", "));
    }

    return methodName + "(" + parameterTypes + ")";
  }

  /**
   * @param id The unique test identifier.
   * @return A string representation of the current invocation (might be {@code null}).
   */
  static String invocation(TestIdentifier identifier, UniqueId id) {

    List<UniqueId.Segment> segments = id.getSegments();

    if (!segments.isEmpty()) {

      UniqueId.Segment last = segments.get(segments.size() - 1);

      switch (last.getType()) {
        case "dynamic-test":
          return identifier.getDisplayName();
        case "test-template-invocation":
          return last.getValue().replace("#", "");
      }
    }

    return null;
  }
}
