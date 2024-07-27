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

import com.github.sbt.junit.jupiter.internal.options.Options;
import sbt.testing.Logger;

import java.text.MessageFormat;

/**
 *
 * @author Michael Aichler
 * @author Original JUnit Interface Developers
 */
public class TestLogger {

    public enum Level {
        INFO, ERROR, DEBUG, WARN
    }

    private final ColorTheme colorTheme;
    private final Configuration configuration;
    private final Logger[] loggers;
    private final Options options;

    TestLogger(Logger[] loggers, Configuration configuration) {

        this.loggers = loggers;
        this.configuration = configuration;
        this.options = configuration.getOptions();
        this.colorTheme = configuration.getColorTheme();
    }

    /**
     * Provide a debug message to available loggers.
     *
     * @param pattern The debug message pattern.
     * @param arguments An optional list of pattern arguments
     */
    public void debug(String pattern, Object... arguments) {

        log(Level.DEBUG, pattern, arguments);
    }

    /**
     * Provide an error message to available loggers.
     *
     * @param pattern The debug message pattern.
     * @param arguments An optional list of pattern arguments
     */
    public void error(String pattern, Object... arguments) {

        log(Level.ERROR, pattern, arguments);
    }

    /**
     * Provide an error message with to available loggers with an optional
     * exception stacktrace and highlighted test-class.
     *
     * @param testClassName The name of the class where the error occurred.
     * @param message The error message.
     * @param t The throwable which describes the error.
     */
    public void error(String testClassName, String message, Throwable t) {

        log(Level.ERROR, message);

        if (null == t) {
            return;
        }

        if (t instanceof AssertionError) {
            if (!options.isAssertLogEnabled()) {
                return;
            }
        }

        logStackTrace(testClassName, t);
    }

    /**
     * Provide an error message to available loggers.
     *
     * @param pattern The debug message pattern.
     * @param arguments An optional list of pattern arguments
     */
    public void info(String pattern, Object... arguments) {

        log(Level.INFO, pattern, arguments);
    }

    /**
     * Provide an error message to available loggers.
     *
     * @param pattern The debug message pattern.
     * @param arguments An optional list of pattern arguments
     */
    public void warn(String pattern, Object... arguments) {

        log(Level.WARN, pattern, arguments);
    }

    /**
     * Provide a log message with the specified level to available loggers.
     *
     * @param level The log level under which the given message should be posted.
     * @param message The log message pattern (see {@link MessageFormat}).
     * @param args An optional list of pattern arguments.
     */
    private void log(Level level, String message, Object... args) {

        if (args.length > 0) {
            message = MessageFormat.format(message, args);
        }

        for (Logger logger : loggers) {

            if (!logger.ansiCodesSupported()) {
                if (options.isColorsEnabled()) {
                    message = Color.filter(message);
                }
            }

            switch (level) {
                case DEBUG:
                    logger.debug(message);
                    break;
                case ERROR:
                    logger.error(message);
                    break;
                case INFO:
                    logger.info(message);
                    break;
                case WARN:
                    logger.warn(message);
                    break;
            }
        }
    }

    private void logStackTrace(String testClassName, Throwable t)
    {
        StackTraceElement[] trace = t.getStackTrace();
        String testFileName = options.isColorsEnabled() ? findTestFileName(trace, testClassName) : null;
        logStackTracePart(trace, trace.length-1, 0, t, testClassName, testFileName);
    }

    private void logStackTracePart(StackTraceElement[] trace, int m, int framesInCommon, Throwable t,
                                   String testClassName, String testFileName)
    {
        final int m0 = m;
        int top = 0;
        for(int i=top; i<=m; i++)
        {
            if(trace[i].toString().startsWith("org.junit.")
                    || trace[i].toString().startsWith("org.hamcrest."))
            {
                if(i == top) top++;
                else
                {
                    m = i-1;
                    while(m > top)
                    {
                        String s = trace[m].toString();
                        if(!s.startsWith("java.lang.reflect.")
                                && !s.startsWith("sun.reflect."))
                            break;
                        m--;
                    }
                    break;
                }
            }
        }

        for(int i=top; i<=m; i++) {
            error("    at " + stackTraceElementToString(trace[i], testClassName, testFileName));
        }

        if(m0 != m)
        {
            // skip junit-related frames
            error("    ...");
        }
        else if(framesInCommon != 0)
        {
            // skip frames that were in the previous trace too
            error("    ... " + framesInCommon + " more");
        }

        logStackTraceAsCause(trace, t.getCause(), testClassName, testFileName);
    }

    private void logStackTraceAsCause(StackTraceElement[] causedTrace, Throwable t,
                                      String testClassName, String testFileName)
    {
        if(t == null) return;
        StackTraceElement[] trace = t.getStackTrace();
        int m = trace.length - 1, n = causedTrace.length - 1;
        while(m >= 0 && n >= 0 && trace[m].equals(causedTrace[n]))
        {
            m--;
            n--;
        }
        error("Caused by: " + t);
        logStackTracePart(trace, m, trace.length-1-m, t, testClassName, testFileName);
    }

    private String findTestFileName(StackTraceElement[] trace, String testClassName)
    {
        for(StackTraceElement e : trace)
        {
            String cln = e.getClassName();
            if(testClassName.equals(cln)) return e.getFileName();
        }
        return null;
    }

    private String stackTraceElementToString(StackTraceElement e, String testClassName,
                                             String testFileName)
    {
        boolean highlight = options.isColorsEnabled() && (
                testClassName.equals(e.getClassName()) ||
                        (testFileName != null && testFileName.equals(e.getFileName()))
        );


        StringBuilder b = new StringBuilder();
        b.append(configuration.decodeName(e.getClassName() + '.' + e.getMethodName()));
        b.append('(');

        if (e.isNativeMethod()) {
            Color nativeMethod = highlight ? colorTheme.nativeMethod() : Color.NONE;
            b.append(nativeMethod.format("Native Method"));
        }
        else if (null == e.getFileName()) {
            Color unknownSource = highlight ? colorTheme.unknownSource() : Color.NONE;
            b.append(unknownSource.format("Unknown Source"));
        }
        else {
            Color testFile = highlight ? colorTheme.testFile() : Color.NONE;
            b.append(testFile.format(e.getFileName()));
            if (e.getLineNumber() >= 0) {
                Color lineNumber = highlight ? colorTheme.testFileLineNumber() : Color.NONE;
                b.append(':');
                b.append(lineNumber.format(String.valueOf(e.getLineNumber())));
            }
        }

        b.append(')');
        return b.toString();
    }

}
