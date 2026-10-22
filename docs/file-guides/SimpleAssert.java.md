# `SimpleAssert.java` — File Guide

## Purpose

`SimpleAssert` is a lightweight, zero-dependency unit testing assertion utility located in `src/test/java/com/smartroute/testutil/`. It provides standard test assertion methods (`assertEquals`, `assertTrue`, `assertFalse`, `assertNotNull`, `assertThrows`, `assertDoesNotThrow`) using pure Java standard library constructs.

## Where It Is Used

- **Package:** `com.smartroute.testutil`
- **Test Suites:** Used by `CampusGraphTest`, `CampusDataMetadataTest`, and `OsmXmlCampusDataLoaderTest`.
- **Portability:** Enables test execution both under Maven Surefire runners and directly in environments without pre-installed testing frameworks.

## Explicit Imports and Purpose

- `java.util.Objects`: Null-safe object comparison (`Objects.equals(expected, actual)`).

## Key Methods

| Method | Role & Description |
|---|---|
| `assertTrue(condition, message)` | Throws `AssertionError` if condition is false. |
| `fail(message)` | Unconditionally throws `AssertionError` with the given message. |
| `assertEquals(expected, actual, delta, message)` | Validates double equality within specified floating-point delta. |
| `assertThrows(expectedType, executable, message)` | Validates that an executable lambda throws an instance of `expectedType`. |
| `assertDoesNotThrow(executable, message)` | Validates that an executable lambda runs without throwing an exception. |

## Viva Discussion Points

- *Why provide a standalone assertion utility?*  
  It guarantees that automated tests can run in any standard Java environment without requiring external build tools or network-downloaded JARs.

