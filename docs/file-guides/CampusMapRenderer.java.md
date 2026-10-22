# `CampusMapRenderer.java` — File Guide

## Purpose

`CampusMapRenderer` is the vector map renderer for the VIT Vellore Smart Route Finder application. It is responsible for projecting real-world geographic coordinates (latitude and longitude) to screen canvas coordinates while preserving the local aspect ratio, rendering campus paths, drawing route/MST overlays, rendering campus landmark markers, supporting interactive smooth zoom and pan controls, and displaying the required OpenStreetMap attribution notice.

## Where It Is Used

- **Package:** `com.smartroute.ui`
- **Application View:** Instantiated by `SmartRouteFinderGUI` and attached to the main JavaFX `Canvas`.
- **User Interactions:** Invoked whenever the canvas resizes, selections change, routes are calculated, the user zooms (via toolbar or mouse wheel), pans (via mouse drag), or clicks on the map.

## Explicit Imports and Purpose

- `com.smartroute.data.CampusDataMetadata`: Supplies dataset bounding box coordinates and attribution text.
- `com.smartroute.model.CampusGraph`: Graph model holding vertices, edges, and destination POIs.
- `com.smartroute.model.GraphNode`: Graph vertex providing geographic coordinates `(latitude, longitude)`.
- `com.smartroute.model.Location`: Selectable campus landmark with display name and coordinates.
- `com.smartroute.model.Road`: Road edge providing connectivity, distance, highway type, and name.
- `java.util.ArrayList`: Dynamic list for path nodes.
- `java.util.Collections`: Unmodifiable and empty collections.
- `java.util.HashSet`: Set for deduplicating rendered road segments.
- `java.util.List`: Interface for route node IDs and MST edges.
- `java.util.Set`: Set interface for edge keys.
- `javafx.geometry.VPos`: Vertical text alignment for attribution and zoom badges.
- `javafx.scene.canvas.Canvas`: JavaFX drawing canvas surface.
- `javafx.scene.canvas.GraphicsContext`: 2D rendering context for line, shape, and text drawing.
- `javafx.scene.paint.Color`: Color styling for paths, overlays, badges, and markers.
- `javafx.scene.text.Font`, `FontWeight`, `TextAlignment`: Map typography and label alignment.

## Geographic Projection Algorithm with Zoom and Pan

To project spherical geographic coordinates onto a 2D plane without vertical stretching or distortion, an equirectangular projection with a local cosine aspect ratio correction is applied, followed by zoom scaling and pan translation:

$$\phi_{\text{mid}} = \frac{\phi_{\text{min}} + \phi_{\text{max}}}{2}$$
$$\text{relX} = (\lambda - \lambda_{\text{min}}) \cdot \cos(\phi_{\text{mid}})$$
$$\text{relY} = \phi_{\text{max}} - \phi$$
$$\text{scale} = \min\left(\frac{W_{\text{draw}}}{\text{spanX}}, \frac{H_{\text{draw}}}{\text{spanY}}\right)$$

$$\text{pixelX} = C_x + (\text{baseX} - C_x) \cdot \text{zoomFactor} + \text{panOffsetX}$$
$$\text{pixelY} = C_y + (\text{baseY} - C_y) \cdot \text{zoomFactor} + \text{panOffsetY}$$

Where:
- $\phi$ is latitude, $\lambda$ is longitude
- $(C_x, C_y)$ is the center of the canvas
- $\text{zoomFactor}$ is clamped between 0.5x and 10.0x
- $(\text{panOffsetX}, \text{panOffsetY})$ are pixel translations from user dragging

## Interactive Zoom and Pan Methods

| Method | Description |
|---|---|
| `zoomIn()` | Multiplies current zoom by 1.25 (up to 10.0x maximum). |
| `zoomOut()` | Multiplies current zoom by 0.80 (down to 0.5x minimum). |
| `resetView()` | Resets `zoomFactor` to 1.0 and resets pan offsets to `(0, 0)`. |
| `zoomAt(mouseX, mouseY, multiplier)` | Zooms with pivot centered at the mouse cursor position so the point under the cursor stays fixed. |
| `pan(deltaX, deltaY)` | Translates the canvas viewport by the given pixel deltas during mouse dragging. |

## Rendering Layers (Back-to-Front)

1. **Background Fill:** Fills canvas with neutral slate-50 background (`#f8fafc`).
2. **Base Road Network:**
   - Roads (service, residential, living street, unclassified): width scaled dynamically with `zoomFactor` (`#cbd5e1`).
   - Pedestrian crossings (`highway=crossing`): highlighted in dark slate (`#64748b`).
   - Pedestrian walkways, paths & steps: width scaled dynamically (`#94a3b8`).
3. **Spanning Tree / Forest Overlay:** Forest green lines (`#16a34a`), width scaled dynamically, rendered when `showMst == true`.
4. **Shortest Route Overlay:** Orange-red lines (`#ea580c`) with intermediate junction step dots (`#c2410c`), rendered when a Dijkstra route is present.
5. **Campus Landmarks & POIs:**
   - Connected locations: Blue circles (`#2563eb`) with white border.
   - Selected start: Green marker (`#16a34a`), size 18px.
   - Selected destination: Red marker (`#dc2626`), size 18px.
   - Unconnected locations: Slate grey (`#94a3b8`), size 8px.
   - Prominent facility labels always visible; all landmark labels become visible when `zoomFactor >= 1.4`.
6. **OpenStreetMap Attribution Badge:**
   - Semitransparent white badge in bottom-right corner reading `"Map data © OpenStreetMap contributors (ODbL)"`.
7. **Interactive Zoom Indicator Badge:**
   - Top-right rounded badge displaying the current zoom percentage (e.g. `Zoom: 100%`).

## Nearest Landmark Hit Testing

`findNearestLocation(double pixelX, double pixelY, double maxDistancePixels)` iterates through all selectable campus destinations, converts their geographic coordinates to current canvas pixels (respecting zoom and pan), and returns the closest location within the tolerance threshold (e.g. 25 pixels).

## Viva Discussion Points

- *Why do we multiply longitude difference by $\cos(\text{latitude})$ during projection?*  
  Lines of longitude converge toward the poles. At VIT Vellore's latitude ($\approx 12.97^\circ\text{ N}$), one degree of longitude covers approximately $\cos(12.97^\circ) \approx 0.9745$ the ground distance of one degree of latitude. Multiplying by this factor prevents horizontal stretching and preserves the true shape of the campus.
- *How does mouse-pivot zooming work?*  
  When zooming at `(mouseX, mouseY)`, the displacement from the canvas center is adjusted proportionally by the zoom ratio: `panOffsetX = mouseX - Cx - (mouseX - Cx - panOffsetX) * ratio`. This ensures the geographic point under the mouse cursor remains static while zooming.
- *How does vector rendering compare to raster tile rendering?*  
  Vector rendering paints lines directly from our local XML geometry without downloading bitmap image tiles over the internet, keeping the application 100% offline, lightweight, and responsive.
