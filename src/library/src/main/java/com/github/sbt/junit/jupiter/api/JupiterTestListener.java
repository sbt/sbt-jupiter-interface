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
package com.github.sbt.junit.jupiter.api;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.launcher.TestExecutionListener;
import sbt.testing.TaskDef;

/**
 * Extended test execution listener.
 *
 * @author Michael Aichler
 */
public interface JupiterTestListener extends TestExecutionListener {

    /**
     * Called when the execution of a test task failed.
     *
     * @param className The class-name as reported by {@link TaskDef#fullyQualifiedName()}.
     * @param throwable The throwable which caused test execution to fail.
     */
    default void executionFailed(String className, Throwable throwable) {

    }

    /**
     * Called when a test has been filtered.
     *
     * @param descriptor The descriptor of the filtered test.
     * @param reason A string which describes why this test was filtered.
     */
    default void executionFiltered(TestDescriptor descriptor, String reason) {

    }
}
