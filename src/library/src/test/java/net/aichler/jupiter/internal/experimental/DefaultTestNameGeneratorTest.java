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
package net.aichler.jupiter.internal.experimental;

import jupiter.TestHelper;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Michael Aichler
 */
@RunWith(Enclosed.class)
public class DefaultTestNameGeneratorTest {

    public static class WithTestPlanAndIdentifier {

        TestHelper testHelper = new TestHelper();

        @Test
        public void should() {

            testHelper.loadTestClass("jupiter.samples.SimpleTests");
            TestIdentifier testIdentifier = testHelper.findByName("firstTestMethod()");

            TestName name = create(testHelper.testPlan(), testIdentifier);
            assertThat(name.technicalName(), equalTo("jupiter.samples.SimpleTests#firstTestMethod()"));
        }
    }

    static TestName create(TestPlan testPlan, TestIdentifier identifier) {

        return new DefaultTestNameGenerator().of(testPlan, identifier);
    }
}