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
package net.aichler.jupiter.internal.filter;

import net.aichler.jupiter.internal.event.Dispatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Michael Aichler
 */
@RunWith(MockitoJUnitRunner.class)
public class TestFilterTest {

    @Mock
    Dispatcher dispatcher;


    @Test
    public void testFindMatchingPattern() {

        String testName;
        TestFilter filter = newTestFilter("basic.FooTest#testFoo", "basic.BarTest#testBar");

        testName = "basic.FooTest#testFoo";
        assertThat(testName, filter.findMatchingPattern(testName).isPresent(), is(true));

        testName = "basic.BarTest#testBar";
        assertThat(testName, filter.findMatchingPattern(testName).isPresent(), is(true));

        testName = "basic.FooBarTest#testFooBar";
        assertThat(testName, filter.findMatchingPattern(testName).isPresent(), is(false));
    }

    @Test
    public void testFindMatchingResult() {

        String testName;
        TestFilter filter = newTestFilter("basic.FooTest#testFoo", "basic.BarTest#testBar");

        testName = "basic.FooTest#testFoo";
        assertThat(testName, filter.findMatchingResult(testName).included(), is(true));

        testName = "basic.BarTest#testBar";
        assertThat(testName, filter.findMatchingResult(testName).included(), is(true));

        testName = "basic.FooBarTest#testFooBar";
        assertThat(testName, filter.findMatchingResult(testName).included(), is(false));
    }

    /**
     * Creates a new glob filter from the specified patterns.
     *
     * @param patterns The test filter patterns.
     * @return A new glob filter.
     */
    TestFilter newTestFilter(String... patterns) {

        return new TestFilter(new HashSet<>(asList(patterns)), dispatcher);
    }
}