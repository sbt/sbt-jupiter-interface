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
package com.github.sbt.junit.jupiter.internal.filter;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.sbt.junit.jupiter.internal.event.Dispatcher;
import java.util.HashSet;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.platform.engine.UniqueId;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** @author Michael Aichler */
@RunWith(MockitoJUnitRunner.class)
public class GlobFilterTest {

  @Mock Dispatcher dispatcher;

  @Test
  public void shouldMatchWildcardPattern() {

    GlobFilter filter = newGlobFilter("basic.*");

    String testName = "basic.FooTest";
    assertThat(testName, filter.findMatchingPattern(testName).isPresent(), is(true));

    testName = "basic.FooTest#someTest()";
    assertThat(testName, filter.findMatchingPattern(testName).isPresent(), is(true));

    testName = "failure.AssumptionsTest";
    assertThat(testName, filter.findMatchingPattern(testName).isPresent(), is(false));
  }

  @Test
  @Ignore
  public void shouldSkipEngineWhenConvertingUniqueIds() {

    GlobFilter filter = newGlobFilter("");
    filter.toTestName(UniqueId.parse(""));
  }

  /**
   * Creates a new glob filter from the specified patterns.
   *
   * @param patterns The test filter patterns.
   * @return A new glob filter.
   */
  GlobFilter newGlobFilter(String... patterns) {

    return new GlobFilter(new HashSet<>(asList(patterns)), dispatcher);
  }
}
