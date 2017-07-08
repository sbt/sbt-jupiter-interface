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

import net.aichler.jupiter.internal.JupiterRunner;
import sbt.testing.Fingerprint;
import sbt.testing.Framework;
import sbt.testing.Runner;

/**
 * Framework entry point to sbt-testing.
 *
 * @author Michael Aichler
 */
@SuppressWarnings("unused")
public class JupiterFramework implements Framework {

    private static final StreamPair system = new StreamPair(System.out, System.err);
    private static final Fingerprint[] FINGERPRINTS = new Fingerprint[] {
            new JupiterTestFingerprint()
    };

    /**
     * @return The human-friendly name of the Jupiter test framework.
     */
    @Override
    public String name() {

        return "Jupiter";
    }

    @Override
    public Fingerprint[] fingerprints() {

        return FINGERPRINTS;
    }

    @Override
    public Runner runner(String[] args, String[] remoteArgs, ClassLoader testClassLoader) {

        return new JupiterRunner(args, remoteArgs, testClassLoader, system);
    }
}
