package sample;

import org.junit.Test;

public class Junit4Test {

    @Test
    public void shouldNotRun() {
        Reporter.report("junit4-test");
    }
}
