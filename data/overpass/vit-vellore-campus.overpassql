/*
 * Smart Route Finder — Overpass Turbo Query for VIT Vellore Campus
 *
 * Bounding Box (South, West, North, East): 12.9640, 79.1500, 12.9790, 79.1670
 * Area: Vellore Institute of Technology (VIT), Katpadi, Vellore, Tamil Nadu, India.
 *
 * Instructions for Manual Snapshot Export (Stage 2):
 * 1. Open https://overpass-turbo.eu/
 * 2. Paste this entire query into the code editor.
 *    (Alternatively, replace the coordinate tuples below with {{bbox}} if using interactive map framing).
 * 3. Click "Run" to preview features on the map.
 * 4. Click "Export" -> "Data" -> "raw data (OSM XML)".
 * 5. Save the output as: src/main/resources/data/vit-vellore-campus.osm.xml
 * 6. Record metadata in: src/main/resources/data/campus-data-metadata.properties
 *
 * Note: The Java importer filters candidate ways locally according to docs/osm-data-specification.md.
 */
[out:xml][timeout:90];
(
  // Walkable and access ways within VIT Vellore campus bounds
  way["highway"](12.9640, 79.1500, 12.9790, 79.1670);

  // Named points of interest and entrance nodes
  node["name"](12.9640, 79.1500, 12.9790, 79.1670);
  node["entrance"](12.9640, 79.1500, 12.9790, 79.1670);

  // Named campus blocks, buildings, and amenities
  way["name"](12.9640, 79.1500, 12.9790, 79.1670);
  way["building"](12.9640, 79.1500, 12.9790, 79.1670);
  relation["building"](12.9640, 79.1500, 12.9790, 79.1670);
  relation["amenity"="university"](12.9640, 79.1500, 12.9790, 79.1670);
);
// Recurse down to include all referenced child nodes and way geometries
(._;>;);
out body;
