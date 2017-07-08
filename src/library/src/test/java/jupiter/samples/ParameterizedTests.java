package jupiter.samples;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ObjectArrayArguments;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ParameterizedTests {

    @ParameterizedTest
    @ValueSource(strings = { "Hello", "World" })
    void testWithStringParameter(String argument) {
        assertNotNull(argument);
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 3 })
    void testWithValueSource(int argument) {
        assertNotNull(argument);
    }

    @ParameterizedTest
    @EnumSource(TimeUnit.class)
    void testWithEnumSource(TimeUnit timeUnit) {
        assertNotNull(timeUnit.name());
    }

    @ParameterizedTest
    @MethodSource(names = "stringProvider")
    void testWithSimpleMethodSource(String argument) {
        assertNotNull(argument);
    }

    @SuppressWarnings("unused")
    static Stream<String> stringProvider() {
        return Stream.of("foo", "bar");
    }

    @ParameterizedTest
    @MethodSource(names = "stringAndIntProvider")
    void testWithMultiArgMethodSource(String first, int second) {
        assertNotNull(first);
        assertNotEquals(0, second);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> stringAndIntProvider() {
        return Stream.of(ObjectArrayArguments.create("foo", 1), ObjectArrayArguments.create("bar", 2));
    }

    @ParameterizedTest
    @CsvSource({ "foo, 1", "bar, 2", "'baz, qux', 3" })
    void testWithCsvSource(String first, int second) {
        assertNotNull(first);
        assertNotEquals(0, second);
    }

    @DisplayName("Display name of container")
    @ParameterizedTest(name = "{index} ==> first=''{0}'', second={1}")
    @CsvSource({ "foo, 1", "bar, 2", "'baz, qux', 3" })
    void testWithCustomDisplayNames(String first, int second) {
    }

}
