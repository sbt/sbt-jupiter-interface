package example.foo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ATest {

  @Test
  void testGet() {
    assertEquals("Test", A.get());
  }

}
