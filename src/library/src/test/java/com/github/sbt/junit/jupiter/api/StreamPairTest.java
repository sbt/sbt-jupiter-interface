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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.PrintStream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Michael Aichler
 */
public class StreamPairTest {

    @Rule
    public final ExpectedException thrownException = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfOutIsNull() {

        thrownException.expect(NullPointerException.class);
        thrownException.expectMessage(startsWith("Output stream "));

        new StreamPair(null, mock(PrintStream.class));
    }

    @Test
    public void shouldThrowExceptionIfErrIsNull() {

        thrownException.expect(NullPointerException.class);
        thrownException.expectMessage(startsWith("Error stream "));

        new StreamPair(mock(PrintStream.class), null);
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