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
package com.github.sbt.junit.jupiter.internal.listeners;


import org.junit.Test;

import java.io.PrintStream;

import static com.github.sbt.junit.jupiter.internal.listeners.OutputCapturingTestListener.CapturedOutputStream;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Michael Aichler
 */
public class CapturedOutputStreamTest {


    @Test
    public void shouldFilterNewLinesFromPrintln() {

        CapturedOutputStream buffer = new CapturedOutputStream(this::devNull);

        PrintStream out = new PrintStream(buffer, true);
        out.println("First line");

        assertThat(buffer.output, contains("First line"));
    }

    @Test
    public void shouldFilterNewLinesFromPrint() {

        CapturedOutputStream buffer = new CapturedOutputStream(this::devNull);

        PrintStream out = new PrintStream(buffer, true);
        out.print("First line \nSecond line");

        assertThat(buffer.output, contains(
                "First line ",
                "Second line"
        ));
    }

    private void devNull(String str) {

    }
}
