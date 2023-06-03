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

import sbt.testing.Event;
import sbt.testing.EventHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Objects;

/**
 * Writes dispatch events to a file for testing/debugging purposes.
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 *   SBT> testOnly -- --trace-dispatch-events
 * }
 * </pre>
 *
 * @author Michael Aichler
 */
public class LoggingEventHandler implements EventHandler {

    private final File targetFile;
    private final EventHandler eventHandler;

    /**
     * @param targetFile The target file where to log the events.
     * @param eventHandler The real event handler.
     */
    public LoggingEventHandler(String targetFile, EventHandler eventHandler) {

        this.targetFile = new File(targetFile);
        this.eventHandler = Objects.requireNonNull(eventHandler, "eventHandler");
    }

    @Override
    public void handle(Event event) {

        writeEventToFile(event);
        eventHandler.handle(event);
    }

    private void writeEventToFile(Event event) {

        try (OutputStream out = new FileOutputStream(targetFile, true)) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {
                writer.write(event.toString());
                writer.newLine();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
