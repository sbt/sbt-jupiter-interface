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

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * @author Michael Aichler
 */
class DispatcherSampleTests {


    static class DurationTests {

        @Test
        void longRunningTest() throws Exception {
            Thread.sleep(50);
        }
    }

    static class DynamicTests {

        @TestFactory
        Collection<DynamicTest> test() {
            return Collections.singletonList(
                    dynamicTest("1st dynamic test", () -> {})
            );
        }
    }

    static class ParameterizedTests {

        @ParameterizedTest
        @ValueSource(strings = {"foo"})
        void testValueSourceWithStrings(String value) {}
    }

    static class SingleParamTests {

        @Test
        void testWithSingleParam(TestInfo info) {}
    }

    static class MultipleParamsTests {

        @Test
        void testWithMultipleParams(TestInfo info1, TestInfo info2) {}
    }

    static class NestedTests {

        @Nested
        class First {

            @Test
            void testOfFirstNestedClass() {}
        }
    }

}
