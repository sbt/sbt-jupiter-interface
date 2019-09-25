package jupiter;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class TestOperators {

    @Test
    @Tag("smoke")
    @Tag("development")
    @Tag("integration")
    public void test0() {
        Reporter.report("op-sdi-0");
    }

    @Test
    @Tag("smoke")
    @Tag("development")
    @Tag("integration")
    public void test1() {
        Reporter.report("op-sdi-1");
    }

    @Test
    @Tag("smoke")
    @Tag("production")
    @Tag("integration")
    public void test2() {
        Reporter.report("op-spi");
    }

    @Test
    @Tag("full")
    @Tag("production")
    @Tag("integration")
    public void test3() {
        Reporter.report("op-fpi");
    }
}
