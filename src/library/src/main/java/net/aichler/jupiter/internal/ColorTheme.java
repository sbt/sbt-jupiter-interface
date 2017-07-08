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
package net.aichler.jupiter.internal;

/**
 * Defines a theme for colored output.
 *
 * @author Michael Aichler
 */
public interface ColorTheme {

    default Color errorCount() {
        return Color.RED;
    }

    default Color ignoreCount() {
        return Color.YELLOW;
    }

    default Color info() {
        return Color.BLUE;
    }

    /**
     * @return The color which should be used for the indicator of a successful test.
     */
    default Color successful() {
        return Color.GREEN;
    }

    default Color error() {
        return Color.RED;
    }

    /**
     * @return The color which should be used for test containers.
     */
    default Color container() {
        return Color.YELLOW;
    }

    /**
     * @return The color which should be used for a test method.
     */
    default Color testMethod() {
        return Color.CYAN;
    }

    /**
     * @return The color which should be used for a test factory.
     */
    default Color testFactory() {
        return Color.CYAN;
    }

    /**
     * @return The color which should be used for a test template.
     */
    default Color testTemplate() {
        return Color.CYAN;
    }

    /**
     * @return The color which should be used for a dynamic test.
     */
    default Color dynamicTest() {
        return Color.MAGENTA;
    }

    /**
     * @return The color which should be used for a native method within a stacktrace.
     */
    default Color nativeMethod() {
        return Color.YELLOW;
    }

    /**
     * @return The color which should be used for an unknown source within a stacktrace.
     */
    default Color unknownSource() {
        return Color.YELLOW;
    }

    /**
     * @return The color which should be used for the test file within a stacktrace.
     */
    default Color testFile() {
        return Color.MAGENTA;
    }

    /**
     * @return The color which should be used for the line number of a test file
     *      within a stacktrace.
     */
    default Color testFileLineNumber() {

        return Color.YELLOW;
    }

    default Color normalName1() {
        return Color.YELLOW;
    }

    default Color normalName2() {
        return Color.CYAN;
    }

    default Color normalName3() {
        return Color.YELLOW;
    }

    default Color errorName1() {
        return Color.YELLOW;
    }

    default Color errorName2() {
        return Color.RED;
    }

    default Color errorName3() {
        return Color.YELLOW;
    }
}
