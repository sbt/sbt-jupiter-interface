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
package com.github.sbt.junit.jupiter.internal;

/**
 * Defines an enumeration of available colors.
 *
 * @author Michael Aichler
 */
public enum Color {
  NONE(-1),
  RESET(0),
  BLACK(30),
  RED(31),
  GREEN(32),
  YELLOW(33),
  BLUE(34),
  MAGENTA(35),
  CYAN(36),
  WHITE(37);

  private final String ansiString;

  /** @param ansiCode The ANSI color code. */
  Color(int ansiCode) {
    this.ansiString = (ansiCode > -1) ? "\u001B[" + ansiCode + "m" : "";
  }

  /**
   * Filter color codes from the specified {@code value}.
   *
   * @param str The string which is to be filtered.
   * @return The string without any color codes.
   */
  public static String filter(String str) {

    if (null == str || !str.contains("\u001B[")) {
      return str;
    }

    StringBuilder b = new StringBuilder();
    int len = str.length();
    for (int i = 0; i < len; i++) {
      char c = str.charAt(i);
      if (c == '\u001B') {
        do {
          i++;
        } while (str.charAt(i) != 'm');
      } else b.append(c);
    }

    return b.toString();
  }

  /**
   * @param value The string which is to be colored.
   * @return The colored string.
   */
  public String format(String value) {

    if (ansiString.isEmpty()) {
      return value;
    }

    return toString() + value + RESET;
  }

  /** @return The ANSI representation of this color. */
  @Override
  public String toString() {

    return this.ansiString;
  }
}
