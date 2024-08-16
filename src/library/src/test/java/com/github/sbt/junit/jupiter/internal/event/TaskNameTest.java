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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.runner.RunWith;

/** @author Michael Aichler */
@RunWith(Enclosed.class)
public class TaskNameTest {

  public static class NestedSuiteIdTest {

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionIfSuiteDoesNotMatch() {

      TaskName.nestedSuiteId("jupiter.SampleTests", "SomeOtherTest$Nested");
    }

    @Test
    public void shouldReturnNullIfEqualsTestSuite() {

      String testSuite = "jupiter.NestedTests";
      String className = "jupiter.NestedTests";

      String result = TaskName.nestedSuiteId(testSuite, className);
      assertThat(result, nullValue());
    }

    @Test
    public void shouldStripTestSuite() {

      String testSuite = "jupiter.NestedTests";
      String className = "jupiter.NestedTests$First";

      String result = TaskName.nestedSuiteId(testSuite, className);
      assertThat(result, equalTo("$First"));
    }
  }

  public static class TestNameTest {

    @Test
    public void withEmptyParameterTypes() {

      String testName = "someTestMethod";

      String result = TaskName.testName(testName, "");
      assertThat(result, equalTo("someTestMethod()"));
    }

    @Test
    public void withSingleParameterType() {

      String testName = "someTestMethod";
      String methodParameterTypes = "java.lang.String";

      String result = TaskName.testName(testName, methodParameterTypes);
      assertThat(result, equalTo("someTestMethod(String)"));
    }

    @Test
    public void withMultipleParameterTypes() {

      String testName = "someTestMethod";
      String methodParameterTypes = "java.lang.String, java.lang.Integer";

      String result = TaskName.testName(testName, methodParameterTypes);
      assertThat(result, equalTo("someTestMethod(String, Integer)"));
    }
  }

  public static class InvocationTest {

    @Test
    public void shouldFindDynamicTest() {

      UniqueId id = UniqueId.root("method", "someTestMethod").append("dynamic-test", "#1");
      TestDescriptor descriptor = new DummyTestDescriptor(id, "myDynamicTest");
      TestIdentifier identifier = TestIdentifier.from(descriptor);

      String result = TaskName.invocation(identifier, id);
      assertThat(result, equalTo("myDynamicTest"));
    }

    @Test
    public void shouldFindTestTemplateInvocation() {

      UniqueId id =
          UniqueId.root("method", "someTestMethod").append("test-template-invocation", "#1");
      TestDescriptor descriptor = new DummyTestDescriptor(id, "myTestTemplateInvocation");
      TestIdentifier identifier = TestIdentifier.from(descriptor);

      String result = TaskName.invocation(identifier, id);
      assertThat(result, equalTo("1"));
    }

    @Test
    public void shouldReturnNullOtherwise() {

      UniqueId id = UniqueId.root("method", "someTestMethod");
      TestDescriptor descriptor = new DummyTestDescriptor(id, "someTestMethod");
      TestIdentifier identifier = TestIdentifier.from(descriptor);

      String result = TaskName.invocation(identifier, id);
      assertThat(result, nullValue());
    }
  }

  private static class DummyTestDescriptor extends AbstractTestDescriptor {
    protected DummyTestDescriptor(UniqueId uniqueId, String displayName) {
      super(uniqueId, displayName);
    }

    @Override
    public Type getType() {
      return Type.CONTAINER_AND_TEST;
    }
  }
}
