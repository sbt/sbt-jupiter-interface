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
package net.aichler.jupiter.api;

import java.io.PrintStream;
import java.util.Objects;

/**
 * Standard output and error stream pair.
 *
 * @author Michael Aichler
 */
public class StreamPair {

    /**
     * Defines available stream types.
     */
    public enum Type {

        ERR, OUT
    }

    private final PrintStream out;
    private final PrintStream err;

    /**
     * Creates a stream pair from the given output and error print stream.
     *
     * @param out The standard output print stream.
     * @param err the error output print stream.
     */
    public StreamPair(final PrintStream out, final PrintStream err) {

        this.out = Objects.requireNonNull(out, "Output stream must not be null.");
        this.err = Objects.requireNonNull(err, "Error stream must not be null.");
    }

    /**
     * @param type The requested type.
     * @return The print stream of the specified {@code type}.
     */
    public PrintStream get(final Type type) {

        Objects.requireNonNull(type, "Stream type must not be null.");
        return Type.OUT.equals(type) ? out : err;
    }
}
