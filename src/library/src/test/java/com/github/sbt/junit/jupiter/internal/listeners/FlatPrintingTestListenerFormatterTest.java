package com.github.sbt.junit.jupiter.internal.listeners;

import junit.TestRunner;
import com.github.sbt.junit.jupiter.internal.Color;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test output formatting of test identifiers.
 *
 * @author Michael Aichler
 */
@RunWith(Parameterized.class)
public class FlatPrintingTestListenerFormatterTest {

    @Parameters(name = "{index}: {0}/{1}")
    public static Object[][] samples() {

        // @formatter:off

        return new Object[][] {
          { "jupiter.samples.NestedTests",   "testOfFirstNestedClass", "{0}$First#{1}()" },
          { "jupiter.samples.RepeatedTests", "repeatedTest",                   "{0}#{1}():#1" },
          { "jupiter.samples.RepeatedTests", "repeatedTestWithRepetitionInfo", "{0}#{1}(org.junit.jupiter.api.RepetitionInfo):#1" },
          { "jupiter.samples.SimpleTests",   "firstTestMethod",   "{0}#{1}()" },
          { "jupiter.samples.SimpleTests",   "testWithParameter", "{0}#{1}(org.junit.jupiter.api.TestInfo)" },
          { "jupiter.samples.VintageTests",  "vintageTestMethod", "{0}#{1}" },
          { "jupiter.samples.VintageEnclosedTests", "testMethod", "{0}$NestedTest#{1}" },
          { "jupiter.samples.VintageParameterizedTests", "testParameters", "{0}#{1}[A-65]" }
        };

        // @formatter:on
    }

    private final String testClassName;
    private final String testMethodName;
    private final String expectedFormatPattern;

    public FlatPrintingTestListenerFormatterTest(
            String testClassName,
            String testMethodName,
            String expectedFormatPattern) {

        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
        this.expectedFormatPattern = expectedFormatPattern;
    }

    @Test
    public void shouldMatchFormat() {

        final String actualFormat = runTestAndExtractFormattedName(testClassName, testMethodName);
        final String expectedFormat = MessageFormat.format(expectedFormatPattern, testClassName, testMethodName);

        final String actualFormatSansColor = Color.filter(actualFormat);

        assertThat(actualFormatSansColor, equalTo(expectedFormat));
    }

    private String runTestAndExtractFormattedName(String testClassName, String testMethodName) {

        final String testNameGlobPattern = MessageFormat.format("*{0}*", testMethodName);

        final TestRunner testRunner = new TestRunner();
        testRunner.withArgs(testNameGlobPattern);
        testRunner.execute(testClassName);

        final List<String> testInfoOutput = testRunner.logger().byLevel("info");

        final TestOutputLine testOutputLine = testInfoOutput.stream()
                .map(TestOutputLine::parse)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> {
                    String message = "Test not found in output: " + String.join("\n", testInfoOutput);
                    return new AssertionError(message);
                });

        return testOutputLine.formattedName;
    }

    /*
     * Helper class which extracts the formatted test name out of a given logger output.
     */
    static class TestOutputLine {

        private String formattedName;
        private static Pattern RX = Pattern.compile("\\[.*?] Test (.*?) started");

        static TestOutputLine parse(String line) {

            final Matcher m = RX.matcher(line);
            if (!m.matches()) {
                return null;
            }

            TestOutputLine result = new TestOutputLine();
            result.formattedName = m.group(1);
            return result;
        }
    }
}
