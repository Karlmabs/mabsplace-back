package com.mabsplace.mabsplaceback;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleUnitTest {

    @Test
    public void testSimpleAssertion() {
        // Simple unit test that doesn't require Spring context
        String expected = "Hello World";
        String actual = "Hello World";
        assertEquals(expected, actual);
    }

    @Test
    public void testMathOperations() {
        int result = 2 + 2;
        assertEquals(4, result);
    }
}
