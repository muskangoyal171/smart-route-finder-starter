# `OsmXmlCampusDataLoaderTest.java` — File Guide

## Purpose

`OsmXmlCampusDataLoaderTest` is the automated test suite for `OsmXmlCampusDataLoader`. It validates the XML parser using both a controlled synthetic in-memory XML fixture and the real campus XML snapshot `vit-vellore-campus.osm.xml`.

## Where It Is Used

- **Package:** `com.smartroute.data`
- **Execution:** Executed directly via `AllTestsRunner`, `test.ps1`, `test.bat`, or `OsmXmlCampusDataLoaderTest.main()`.

## Explicit Imports and Purpose

- `com.smartroute.model.CampusGraph`: Graph model being tested.
- `com.smartroute.model.Location`: Destination model and snap types.
- `com.smartroute.testutil.SimpleAssert`: Test assertions.
- `java.io.*`: ByteArrayInputStream, File, InputStream.
- `java.nio.charset.StandardCharsets`: UTF-8 stream decoding.
- `java.nio.file.Files`, `Path`: Disk file checks.

## Test Cases Covered

1. `testFixtureParsingAndFiltering()`:
   * Parses synthetic XML containing:
     - 1 eligible `footway` way (nodes 1 -> 2 -> 3).
     - 1 rejected `primary` arterial highway.
     - 1 rejected `foot=no` service road.
     - 1 proximate POI (< 35m) -> asserts `PROXIMITY_SNAP` and connection to node 1.
     - 1 distant POI (> 100m) -> asserts `UNCONNECTED` and no fake edges.
2. `testRealCampusXmlSnapshotLoading()`:
   * Parses the real 1.50 MB campus snapshot `vit-vellore-campus.osm.xml`.
   * Asserts 1,643 graph vertices and 3,586 directed edges (1,793 non-zero segments) after incorporating pedestrian crossings.
   * Asserts 24 connected components (reduced from 31 by crossing unification).
   * Asserts destination count > 50 and confirms *EV Periyar Library* is indexed.
   * Asserts that `validateInvariants()` passes without any errors.
