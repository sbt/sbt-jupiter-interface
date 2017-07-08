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

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Michael Aichler
 */
public class ColorTest {

    @Test
    public void colorNoneShouldNotFormatAnything() {

        String expected = "Some string";
        String actual = Color.NONE.format(expected);

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void shouldFormatWithColor() {

        String value = "Some string";
        String expected = "\u001B[37mSome string\u001B[0m";

        assertThat(Color.WHITE.format(value), equalTo(expected));
    }

    @Test
    public void shouldFilterColors() {

        String value = "\u001B[37mSome string\u001B[0m";
        String expected = "Some string";

        assertThat(Color.filter(value), equalTo(expected));
    }

    @Test
    public void toStringShouldReturnAnsiSequence() {

        assertThat(Color.BLACK.toString(), equalTo("\u001B[30m"));
    }

    @Test
    public void toStringOfColorNoneShouldReturnEmptyString() {

        assertThat(Color.NONE.toString(), equalTo(""));
    }
}