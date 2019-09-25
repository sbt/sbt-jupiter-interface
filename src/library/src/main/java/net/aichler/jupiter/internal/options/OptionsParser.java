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
package net.aichler.jupiter.internal.options;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Michael Aichler
 */
public class OptionsParser {

    private static final String OPT_TESTS = "--tests=";
    private static final String OPT_TRACE_DISPATCH_EVENTS = "--trace-dispatch-events";
    private static final String OPT_DISPLAY_MODE = "--display-mode=";
    private static final String OPT_RUN_LISTENER = "--run-listener=";
    private static final String OPT_INCLUDE_TAGS = "--include-tags=";
    private static final String OPT_EXCLUDE_TAGS = "--exclude-tags=";

    public Options parse(String[] arguments) {

        Options.Builder builder = new Options.Builder();

        for (String arg : arguments) {
            if ("-q".equals(arg))
                builder.withQuiet(true);

            else if ("-v".equals(arg))
                builder.withVerbose(true);

            else if ("-n".equals(arg))
                builder.withColorsEnabled(false);

            else if ("-s".equals(arg))
                builder.withDecodeScalaNames(true);

            else if ("-a".equals(arg))
                builder.withAssertLogEnabled(true);

            else if ("-c".equals(arg))
                builder.withExceptionClassLogEnabled(false);

            else if ("--with-types".equals(arg))
                builder.withTypesEnabled(true);

            else if (arg.startsWith(OPT_DISPLAY_MODE))
                builder.withDisplayMode(toValue(OPT_DISPLAY_MODE, arg));

            else if (arg.startsWith(OPT_TESTS))
                builder.withTestFilters(toSet(OPT_TESTS, arg));

            else if (arg.startsWith(OPT_TRACE_DISPATCH_EVENTS))
                 builder.withTraceDispatchEvents(true);
            
            else if (arg.startsWith(OPT_RUN_LISTENER))
                builder.withRunListener(toValue(OPT_RUN_LISTENER, arg));

            else if (arg.startsWith(OPT_INCLUDE_TAGS))
                builder.withIncludeTags(toSet(OPT_INCLUDE_TAGS, arg));

            else if (arg.startsWith(OPT_EXCLUDE_TAGS))
                builder.withExcludeTags(toSet(OPT_EXCLUDE_TAGS, arg));

            else if (arg.startsWith("-D") && arg.contains("="))
                builder.withSystemProperty(toEntry(arg));

            else if (!arg.startsWith("-") && !arg.startsWith("+"))
                builder.withGlobPattern(arg);

        }

        for (String arg : arguments) {
            if ("+q".equals(arg)) builder.withQuiet(false);
            else if ("+v".equals(arg)) builder.withVerbose(false);
            else if ("+n".equals(arg)) builder.withColorsEnabled(true);
            else if ("+s".equals(arg)) builder.withDecodeScalaNames(false);
            else if ("+a".equals(arg)) builder.withAssertLogEnabled(false);
            else if ("+c".equals(arg)) builder.withExceptionClassLogEnabled(true);
        }

        return builder.build();
    }


    private Map.Entry<String, String> toEntry(String arg) {

        int indexOf = arg.indexOf('=');
        String key = arg.substring(2, indexOf);
        String value = arg.substring(indexOf+1);

        return new AbstractMap.SimpleEntry<>(key, value);
    }

    /**
     * Splits a comma separated list of arguments into a set of strings.
     *
     * @param key parameter name with equal sign (e.g. --include-tags=)
     * @param arg arg
     */
    private Set<String> toSet(String key, String arg) {

        final String arguments = arg.substring(key.length());
        final String[] values = stripQuotes(arguments).split(",");
        return Arrays.stream(values)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(this::stripQuotes)
                .collect(Collectors.toSet());
    }

    private String toValue(String key, String arg) {

        return arg.substring(key.length());
    }

    private final static char DQ = '"';
    private final static char SQ = '\'';

    private String stripQuotes(String s) {

        final int len = s.length();

        if (len > 1) {
            if (DQ == s.charAt(0) && DQ == s.charAt(len - 1)) {
                return s.substring(1, len - 1);
            }

            if (SQ == s.charAt(0) && SQ == s.charAt(len - 1)) {
                return s.substring(1, len - 1);
            }
        }

        return s;
    }
}
