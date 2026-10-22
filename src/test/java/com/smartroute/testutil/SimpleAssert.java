package com.smartroute.testutil;

// Explicit imports with purpose comments
import java.util.Objects; // Used for null-safe object comparison

/**
 * Lightweight assertion utility providing standard test assertions without external library dependencies.
 * <p>
 * This allows automated tests in {@code src/test/java/} to run in any standard Java environment
 * as well as under Maven Surefire test runners.
 * </p>
 */
public final class SimpleAssert {

    private SimpleAssert() {}

    public static void fail(String message) {
        throw new AssertionError("Assertion failed: " + message);
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }

    public static void assertTrue(boolean condition) {
        assertTrue(condition, "Expected true but was false");
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }

    public static void assertFalse(boolean condition) {
        assertFalse(condition, "Expected false but was true");
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("Assertion failed: " + message + " (expected: " + expected + ", actual: " + actual + ")");
        }
    }

    public static void assertEquals(Object expected, Object actual) {
        assertEquals(expected, actual, "Values are not equal");
    }

    public static void assertEquals(double expected, double actual, double delta, String message) {
        if (Math.abs(expected - actual) > delta) {
            throw new AssertionError("Assertion failed: " + message + " (expected: " + expected + ", actual: " + actual + ", delta: " + delta + ")");
        }
    }

    public static void assertEquals(double expected, double actual, double delta) {
        assertEquals(expected, actual, delta, "Double values differ by more than delta");
    }

    public static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError("Assertion failed: " + message + " (object was null)");
        }
    }

    public static void assertNotNull(Object obj) {
        assertNotNull(obj, "Expected non-null object");
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Throwable;
    }

    public static <T extends Throwable> T assertThrows(Class<T> expectedType, ThrowingRunnable executable, String message) {
        try {
            executable.run();
        } catch (Throwable actual) {
            if (expectedType.isInstance(actual)) {
                @SuppressWarnings("unchecked")
                T casted = (T) actual;
                return casted;
            }
            throw new AssertionError("Assertion failed: " + message + " (expected exception " + expectedType.getName() + " but caught " + actual.getClass().getName() + ")", actual);
        }
        throw new AssertionError("Assertion failed: " + message + " (expected exception " + expectedType.getName() + " was not thrown)");
    }

    public static void assertDoesNotThrow(ThrowingRunnable executable, String message) {
        try {
            executable.run();
        } catch (Throwable ex) {
            throw new AssertionError("Assertion failed: " + message + " (unexpected exception thrown: " + ex.getMessage() + ")", ex);
        }
    }
}

