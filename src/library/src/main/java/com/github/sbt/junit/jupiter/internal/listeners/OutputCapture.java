/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.github.sbt.junit.jupiter.internal.listeners;

import com.github.sbt.junit.jupiter.api.StreamPair;
import com.github.sbt.junit.jupiter.api.StreamPair.Type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Objects;
import java.util.Optional;

/**
 * Hijacks the systems standard output and error streams on a per-thread basis
 * and redirects to given streams.
 *
 * @author Original Developers of Apache Geronimo GShell
 * @version $Rev: 725707 $ $Date: 2008-12-11 16:00:30 +0100 (Thu, 11 Dec 2008) $
 */
@SuppressWarnings("WeakerAccess")
public class OutputCapture {

    /**
     * Contains a {@link StreamRegistration} for the current thread if its registered, else null.
     */
    private static final InheritableThreadLocal<StreamRegistration> registrations =
            new InheritableThreadLocal<>();

    /**
     * The previously installed System streams, initialized when installing.
     */
    private static StreamPair previous;

    /**
     * Shared state between multiple threads and class-loaders.
     */
    private static SharedProperty sharedProperty = new SharedProperty();


    /**
     * Possibly installs delegating print streams.
     * <p>
     *     Checks whether the current thread is the first thread to install
     *     output capturing. If {@code true}, the original print streams will
     *     be overwritten by custom, delegating print streams.
     * </p>
     * <p>
     *     In any case this method stores the specified {@code system} stream
     *     pair, so that it can be successfully restored later on.
     * </p>
     *
     * @see #register(PrintStream, PrintStream)
     *
     * @param system A stream pair containing the original system streams.
     */
    public static synchronized void install(StreamPair system) {

        // Capture the original set of streams
        previous = system;

        if (sharedProperty.install()) {

            PrintStream out = new DelegateStream(Type.OUT);
            PrintStream err = new DelegateStream(Type.ERR);

            // Install our streams

            System.setOut(out);
            System.setErr(err);
        }
    }

    /**
     * Possibly restores the original print streams.
     *
     * <p>
     *     Checks whether the current thread is the last thread which previously
     *     installed output capturing. If {@code true}, then the original print
     *     streams will be restored.
     * </p>
     *
     * @see #install(StreamPair)
     */
    public static synchronized void uninstall() {

        if (sharedProperty.uninstall()) {

            System.setOut(previous.get(Type.OUT));
            System.setErr(previous.get(Type.ERR));
        }
    }

    /**
     * Register streams for the current thread.
     *
     * @param out The standard output stream which is to be registered.
     * @param err The error stream which is to be registered.
     * @throws IllegalStateException If output capturing has not been installed.
     */
    public static synchronized void register(final PrintStream out, final PrintStream err) {

        if (null == previous) {
            throw new IllegalStateException("Output capture was not installed.");
        }

        StreamRegistration prev = registration().orElse(null);

        StreamPair pair = new StreamPair(out, err);

        StreamRegistration next = new StreamRegistration(pair, prev);

        registrations.set(next);
    }

    /**
     * Deregister streams for the current thread, and restore the previous if any.
     */
    public static synchronized void deregister() {

        registration().ifPresent(r -> registrations.set(r.previous));
    }

    /**
     * Get the current stream registration.
     */
    private static synchronized Optional<StreamRegistration> registration() {

        return Optional.ofNullable(registrations.get());
    }

    /**
     * Returns the currently registered streams.
     */
    private static synchronized StreamPair current() {

        return registration().map(r -> r.streams).orElse(previous);
    }

    /**
     * Shared state between multiple threads and class-loaders via system properties.
     */
    private static class SharedProperty {

        private static final String NAME = SharedProperty.class.getName();

        /**
         * @return A unique identifier for the current thread.
         */
        String id() {
            return ":" + Thread.currentThread().getId();
        }

        /**
         * @return {@code True}, if output capture should be installed.
         */
        boolean install() {

            String property = Optional.ofNullable(System.getProperty(NAME)).orElse("");
            if (!property.isEmpty()) {
                if (!property.contains(id())) {
                    System.setProperty(NAME, property + id());
                }
                return false;
            }

            System.setProperty(NAME, id());
            return true;
        }

        /**
         * @return {@code True}, if output capture should be uninstalled.
         */
        boolean uninstall() {

            String property = Optional.ofNullable(System.getProperty(NAME)).orElse("");
            if (property.isEmpty()) {
                return false;
            }

            if (property.contains(id())) {
                property = property.replace(id(), "");
                System.setProperty(NAME, property);
            }

            return property.isEmpty();
        }
    }

    /**
     * Stream registration information.
     */
    private static class StreamRegistration {

        final StreamPair streams;
        final StreamRegistration previous;

        StreamRegistration(final StreamPair streams, final StreamRegistration previous) {

            this.streams = Objects.requireNonNull(streams, "streamPair");
            this.previous = previous;
        }
    }

    /**
     * Delegates write calls to the currently registered stream.
     */
    private static class DelegateStream extends PrintStream {

        private final Type type;
        private static final OutputStream NULL_OUTPUT = new ByteArrayOutputStream();

        DelegateStream(final Type type) {

            super(NULL_OUTPUT);
            this.type = Objects.requireNonNull(type);
        }

        @Override
        public void write(final int b) {

            get().write(b);
        }

        @Override
        public void write(final byte b[]) throws IOException {

            get().write(b, 0, b.length);
        }

        @Override
        public void write(final byte[] b, final int off, final int len) {

            get().write(b, off, len);
        }

        @Override
        public void flush() {

            get().flush();
        }

        @Override
        public void close() {

            get().close();
        }

        private PrintStream get() {

            return current().get(type);
        }

        @Override
        public String toString() {

            return "DelegateStream(" + type +
                    ", " + get() +
                    ')';
        }
    }
}