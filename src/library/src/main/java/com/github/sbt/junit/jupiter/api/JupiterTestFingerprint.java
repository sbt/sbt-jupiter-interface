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

import java.util.Objects;
import sbt.testing.AnnotatedFingerprint;

/**
 * A dummy fingerprint implementation used for all discovered tests.
 *
 * @author Michael Aichler
 */
public class JupiterTestFingerprint implements AnnotatedFingerprint {

  /**
   * @return Always {@code false}.
   */
  @Override
  public boolean isModule() {
    return false;
  }

  /**
   * @return The name of this class. This is to ensure that SBT does not find any tests so that we
   *     can use JUnit Jupiter's test discovery mechanism.
   */
  @Override
  public String annotationName() {
    return getClass().getName();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AnnotatedFingerprint) {
      AnnotatedFingerprint f = (AnnotatedFingerprint) obj;
      if (annotationName().equals(f.annotationName())) {
        return isModule() == f.isModule();
      }
    }

    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.annotationName(), this.isModule());
  }
}
