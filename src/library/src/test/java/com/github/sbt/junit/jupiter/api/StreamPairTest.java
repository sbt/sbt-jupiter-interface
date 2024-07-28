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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.PrintStream;
import org.junit.Test;

/** @author Michael Aichler */
public class StreamPairTest {

  @Test
  public void shouldThrowExceptionIfOutIsNull() {
    Exception thrownException =
        assertThrows(
            NullPointerException.class, () -> new StreamPair(null, mock(PrintStream.class)));

    assertThat(thrownException.getMessage(), startsWith("Output stream "));
  }

  @Test
  public void shouldThrowExceptionIfErrIsNull() {
    Exception thrownException =
        assertThrows(
            NullPointerException.class, () -> new StreamPair(mock(PrintStream.class), null));

    assertThat(thrownException.getMessage(), startsWith("Error stream "));
  }

  @Test
  public void shouldGetTypeOut() {
    PrintStream out = mock(PrintStream.class);
    PrintStream err = mock(PrintStream.class);

    PrintStream result = new StreamPair(out, err).get(StreamPair.Type.OUT);
    assertThat(result, equalTo(out));
    assertThat(result, not(equalTo(err)));
  }

  @Test
  public void shouldGetTypeErr() {
    PrintStream out = mock(PrintStream.class);
    PrintStream err = mock(PrintStream.class);

    PrintStream result = new StreamPair(out, err).get(StreamPair.Type.ERR);
    assertThat(result, equalTo(err));
    assertThat(result, not(equalTo(out)));
  }
}
