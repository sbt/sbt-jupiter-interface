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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;

import com.github.sbt.junit.jupiter.internal.event.DispatcherSampleTests.DurationTests;
import com.github.sbt.junit.jupiter.internal.event.DispatcherSampleTests.DynamicTests;
import com.github.sbt.junit.jupiter.internal.event.DispatcherSampleTests.MultipleParamsTests;
import com.github.sbt.junit.jupiter.internal.event.DispatcherSampleTests.NestedTests;
import com.github.sbt.junit.jupiter.internal.event.DispatcherSampleTests.ParameterizedTests;
import com.github.sbt.junit.jupiter.internal.event.DispatcherSampleTests.SingleParamTests;
import java.util.Arrays;
import java.util.List;
import junit.TestRunner;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import sbt.testing.Event;
import sbt.testing.NestedTestSelector;
import sbt.testing.Selector;
import sbt.testing.Status;
import sbt.testing.TestSelector;

/**
 * @author Michael Aichler
 */
public class DispatcherTest {

  @Rule public final TestRunner testRunner = new TestRunner();

  @Test
  public void shouldCalculateDuration() {

    testRunner.execute(DurationTests.class);

    List<Event> result = testRunner.eventHandler().byStatus(Status.Success);

    assertThat(result, hasSize(1));
    assertThat(result.get(0).duration(), greaterThan(0L));
  }

  @Test
  public void shouldReportSimpleMethodParameterType() {

    testRunner.withArgs("-v");
    testRunner.execute(SingleParamTests.class);

    List<Event> result = testRunner.eventHandler().byStatus(Status.Success);

    String suiteName = ".event.DispatcherSampleTests$SingleParamTests";
    String testName = "testWithSingleParam(TestInfo)";

    assertThat(result, hasSize(1));
    assertThat(result, hasItem(fullyQualifiedName(endsWith(suiteName))));
    assertThat(result, hasItem(selector(instanceOf(TestSelector.class))));
    assertThat(result, hasItem(selector(testName(equalTo(testName)))));
  }

  @Test
  public void shouldReportSimpleMethodParameterTypes() {

    testRunner.withArgs("-v");
    testRunner.execute(MultipleParamsTests.class);

    List<Event> result = testRunner.eventHandler().byStatus(Status.Success);

    String suiteName = ".event.DispatcherSampleTests$MultipleParamsTests";
    String testName = "testWithMultipleParams(TestInfo, TestInfo)";

    assertThat(result, hasSize(1));
    assertThat(result, hasItem(fullyQualifiedName(endsWith(suiteName))));
    assertThat(result, hasItem(selector(instanceOf(TestSelector.class))));
    assertThat(result, hasItem(selector(testName(equalTo(testName)))));
  }

  @Test
  public void shouldReportParameterizedTests() {

    testRunner.withArgs("-v");
    testRunner.execute(ParameterizedTests.class);

    List<Event> result = testRunner.eventHandler().byStatus(Status.Success);

    String suiteName = ".event.DispatcherSampleTests$ParameterizedTests";
    String testName = "testValueSourceWithStrings(String):1";

    assertThat(result, hasSize(1));
    assertThat(result, hasItem(fullyQualifiedName(endsWith(suiteName))));
    assertThat(result, hasItem(selector(instanceOf(TestSelector.class))));
    assertThat(result, hasItem(selector(testName(equalTo(testName)))));
  }

  @Test
  public void shouldReportDynamicTests() {

    testRunner.withArgs("-v");
    testRunner.execute(DynamicTests.class);

    List<Event> result = testRunner.eventHandler().byStatus(Status.Success);

    String suiteName = ".event.DispatcherSampleTests$DynamicTests";
    List<String> testNames =
        Arrays.asList(
            "test():1st dynamic test", "test():2nd dynamic test", "test():3rd dynamic test");

    assertThat(result, hasSize(3));
    assertThat(result, hasItem(fullyQualifiedName(endsWith(suiteName))));
    assertThat(result, hasItem(selector(instanceOf(TestSelector.class))));
    for (String testName : testNames) {
      assertThat(result, hasItem(selector(testName(equalTo(testName)))));
    }
  }

  @Test
  public void shouldReportNestedTestsCorrectly() {

    testRunner.withArgs("-v");
    testRunner.execute(NestedTests.class);

    List<Event> result = testRunner.eventHandler().byStatus(Status.Success);

    String suiteName = ".event.DispatcherSampleTests$NestedTests";
    String testName = "testOfFirstNestedClass()";

    assertThat(result, hasSize(1));
    assertThat(result, hasItem(fullyQualifiedName(endsWith(suiteName))));
    assertThat(result, hasItem(selector(instanceOf(NestedTestSelector.class))));
    assertThat(result, hasItem(selector(testName(equalTo(testName)))));
  }

  private FeatureMatcher<Event, String> fullyQualifiedName(Matcher<String> matcher) {
    return new FeatureMatcher<Event, String>(matcher, "fullyQualifiedName", "fullyQualifiedName") {
      @Override
      protected String featureValueOf(Event actual) {
        return actual.fullyQualifiedName();
      }
    };
  }

  private FeatureMatcher<Selector, String> testName(Matcher<String> matcher) {
    return new FeatureMatcher<Selector, String>(matcher, "testName", "testName") {
      @Override
      protected String featureValueOf(Selector actual) {

        if (actual instanceof TestSelector) return ((TestSelector) actual).testName();

        if (actual instanceof NestedTestSelector) return ((NestedTestSelector) actual).testName();

        return null;
      }
    };
  }

  private FeatureMatcher<Event, Selector> selector(Matcher<Selector> matcher) {
    return new FeatureMatcher<Event, Selector>(matcher, "selector", "selector") {
      @Override
      protected Selector featureValueOf(Event actual) {
        return actual.selector();
      }
    };
  }
}
