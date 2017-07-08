package jupiter;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class TestOne {

    @Test
    @Tag("fast")
    public void fast() {
        Reporter.report("one-fast");
    }

    @Test
    @Tag("slow")
    public void slow() {
        Reporter.report("one-slow");
    }

    @Test
    public void none() {
        Reporter.report("one-none");
    }

}