package com.smartroute.ui;

// Explicit imports with purpose comments
import com.smartroute.data.CampusDataMetadata;        // Metadata for bounds and attribution text
import com.smartroute.model.CampusGraph;               // Graph model holding nodes, edges, and destinations
import com.smartroute.model.GraphNode;                 // Vertex model for coordinate retrieval
import com.smartroute.model.Location;                  // Location model for landmark rendering
import com.smartroute.model.Road;                      // Road edge model for vector drawing
import java.util.ArrayList;                            // Dynamic list for route node IDs
import java.util.Collections;                          // Empty collection utilities
import java.util.HashSet;                              // Set for deduplicating rendered road segments
import java.util.List;                                 // List interface for route and tree edges
import java.util.Set;                                  // Set interface
import javafx.geometry.VPos;                           // Vertical text alignment
import javafx.scene.canvas.Canvas;                     // JavaFX drawing canvas
import javafx.scene.canvas.GraphicsContext;            // 2D rendering context
import javafx.scene.paint.Color;                       // Color styling
import javafx.scene.text.Font;                         // Typography for map labels
import javafx.scene.text.FontWeight;                   // Font weight styling
import javafx.scene.text.TextAlignment;                // Text alignment styling

/**
 * Renders real OpenStreetMap vector geometry and route overlays onto a JavaFX {@link Canvas}.
 * <p>
 * <b>Key Responsibilities:</b>
 * <ul>
 *   <li><b>Geographic Projection:</b> Converts geographic coordinates (latitude, longitude)
 *       into Canvas pixel coordinates {@code (x, y)} while preserving the local aspect ratio.</li>
 *   <li><b>Vector Rendering:</b> Draws campus walkways, roads, and markers from {@link CampusGraph}.</li>
 *   <li><b>Overlay Management:</b> Renders Dijkstra shortest path (orange-red) and MST/MSF (forest green).</li>
 *   <li><b>Attribution Display:</b> Renders mandatory OpenStreetMap contributor attribution on the map.</li>
 *   <li><b>Interaction Support:</b> Maps Canvas click coordinates to the nearest selectable campus place.</li>
 * </ul>
 * </p>
 */
public class CampusMapRenderer {

    private final CampusGraph graph;
    private final CampusDataMetadata metadata;
    private final Canvas canvas;

    private double minLat;
    private double maxLat;
    private double minLon;
    private double maxLon;
    private final double padding = 35.0;

    // Interactive zoom and pan states
    private double zoomFactor = 1.0;
    private double panOffsetX = 0.0;
    private double panOffsetY = 0.0;

    // Display overlay states
    private List<Long> routeNodeIds;
    private List<Road> mstEdges;
    private boolean showMst;
    private Location selectedStart;
    private Location selectedDestination;

    /**
     * Constructs the map renderer.
     *
     * @param graph    the loaded campus graph
     * @param metadata the loaded snapshot provenance metadata
     * @param canvas   the target JavaFX Canvas to draw upon
     */
    public CampusMapRenderer(CampusGraph graph, CampusDataMetadata metadata, Canvas canvas) {
        if (graph == null) {
            throw new IllegalArgumentException("CampusGraph cannot be null");
        }
        if (canvas == null) {
            throw new IllegalArgumentException("Canvas cannot be null");
        }
        this.graph = graph;
        this.metadata = metadata;
        this.canvas = canvas;

        this.routeNodeIds = Collections.emptyList();
        this.mstEdges = Collections.emptyList();
        this.showMst = false;

        calculateBounds();
    }

    private void calculateBounds() {
        if (metadata != null && metadata.getBboxSouth() < metadata.getBboxNorth()) {
            this.minLat = metadata.getBboxSouth();
            this.maxLat = metadata.getBboxNorth();
            this.minLon = metadata.getBboxWest();
            this.maxLon = metadata.getBboxEast();
        } else {
            // Compute bounds dynamically from graph nodes
            this.minLat = Double.MAX_VALUE;
            this.maxLat = -Double.MAX_VALUE;
            this.minLon = Double.MAX_VALUE;
            this.maxLon = -Double.MAX_VALUE;

            for (GraphNode node : graph.getAllNodes().values()) {
                minLat = Math.min(minLat, node.getLatitude());
                maxLat = Math.max(maxLat, node.getLatitude());
                minLon = Math.min(minLon, node.getLongitude());
                maxLon = Math.max(maxLon, node.getLongitude());
            }

            // Fallback defaults if graph has no nodes
            if (minLat >= maxLat) {
                minLat = 12.9640;
                maxLat = 12.9790;
                minLon = 79.1500;
                maxLon = 79.1670;
            }
        }
    }

    /**
     * Converts longitude to Canvas pixel X coordinate preserving aspect ratio, zoom, and pan.
     */
    public double toPixelX(double lon) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        if (width <= 0 || height <= 0) return 0.0;

        double drawW = width - 2 * padding;
        double drawH = height - 2 * padding;

        double midLat = (minLat + maxLat) / 2.0;
        double aspectCos = Math.cos(Math.toRadians(midLat));

        double spanX = (maxLon - minLon) * aspectCos;
        double spanY = maxLat - minLat;

        double scale = Math.min(drawW / spanX, drawH / spanY);
        double offsetX = padding + (drawW - spanX * scale) / 2.0;

        double relX = (lon - minLon) * aspectCos;
        double baseX = offsetX + relX * scale;

        // Apply interactive zoom and pan relative to canvas center
        double centerX = width / 2.0;
        return centerX + (baseX - centerX) * zoomFactor + panOffsetX;
    }

    /**
     * Converts latitude to Canvas pixel Y coordinate preserving aspect ratio, zoom, and pan.
     */
    public double toPixelY(double lat) {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        if (width <= 0 || height <= 0) return 0.0;

        double drawW = width - 2 * padding;
        double drawH = height - 2 * padding;

        double midLat = (minLat + maxLat) / 2.0;
        double aspectCos = Math.cos(Math.toRadians(midLat));

        double spanX = (maxLon - minLon) * aspectCos;
        double spanY = maxLat - minLat;

        double scale = Math.min(drawW / spanX, drawH / spanY);
        double offsetY = padding + (drawH - spanY * scale) / 2.0;

        // Latitude increases upwards, Canvas Y increases downwards
        double relY = maxLat - lat;
        double baseY = offsetY + relY * scale;

        // Apply interactive zoom and pan relative to canvas center
        double centerY = height / 2.0;
        return centerY + (baseY - centerY) * zoomFactor + panOffsetY;
    }

    /**
     * Zooms in by a factor of 1.25.
     */
    public void zoomIn() {
        setZoom(zoomFactor * 1.25);
    }

    /**
     * Zooms out by a factor of 0.80.
     */
    public void zoomOut() {
        setZoom(zoomFactor * 0.80);
    }

    /**
     * Resets the zoom level to 100% and centers the map view.
     */
    public void resetView() {
        this.zoomFactor = 1.0;
        this.panOffsetX = 0.0;
        this.panOffsetY = 0.0;
        draw();
    }

    /**
     * Sets the absolute zoom factor clamped between 0.5 (zoomed out) and 10.0 (zoomed in).
     *
     * @param newZoom the desired zoom factor
     */
    public void setZoom(double newZoom) {
        this.zoomFactor = Math.max(0.5, Math.min(10.0, newZoom));
        draw();
    }

    /**
     * Zooms centered at a specific mouse cursor position.
     *
     * @param mouseX         pivot X coordinate in pixels
     * @param mouseY         pivot Y coordinate in pixels
     * @param zoomMultiplier relative zoom change factor
     */
    public void zoomAt(double mouseX, double mouseY, double zoomMultiplier) {
        double newZoom = Math.max(0.5, Math.min(10.0, zoomFactor * zoomMultiplier));
        if (Math.abs(newZoom - zoomFactor) < 0.0001) return;

        double width = canvas.getWidth();
        double height = canvas.getHeight();
        if (width <= 0 || height <= 0) return;

        double centerX = width / 2.0;
        double centerY = height / 2.0;

        double ratio = newZoom / zoomFactor;
        panOffsetX = mouseX - centerX - (mouseX - centerX - panOffsetX) * ratio;
        panOffsetY = mouseY - centerY - (mouseY - centerY - panOffsetY) * ratio;
        zoomFactor = newZoom;
        draw();
    }

    /**
     * Pans the viewport by the specified delta pixel offsets.
     *
     * @param deltaX horizontal displacement in pixels
     * @param deltaY vertical displacement in pixels
     */
    public void pan(double deltaX, double deltaY) {
        this.panOffsetX += deltaX;
        this.panOffsetY += deltaY;
        draw();
    }

    public double getZoomFactor() {
        return zoomFactor;
    }

    public double getPanOffsetX() {
        return panOffsetX;
    }

    public double getPanOffsetY() {
        return panOffsetY;
    }

    /**
     * Finds the nearest selectable campus location to a given canvas pixel coordinate.
     *
     * @param pixelX             clicked X coordinate on the canvas
     * @param pixelY             clicked Y coordinate on the canvas
     * @param maxDistancePixels  maximum click tolerance radius in pixels
     * @return the closest Location within the radius, or null if none is close enough
     */
    public Location findNearestLocation(double pixelX, double pixelY, double maxDistancePixels) {
        Location best = null;
        double closestDist = maxDistancePixels;

        for (Location loc : graph.getAllLocations()) {
            double locX = toPixelX(loc.getLongitude());
            double locY = toPixelY(loc.getLatitude());

            double dist = Math.hypot(pixelX - locX, pixelY - locY);
            if (dist < closestDist) {
                closestDist = dist;
                best = loc;
            }
        }
        return best;
    }

    /**
     * Redraws all layers onto the Canvas.
     */
    public void draw() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // 1. Background
        gc.setFill(Color.web("#f8fafc"));
        gc.fillRect(0, 0, w, h);

        // 2. Base Road Network
        drawRoadNetwork(gc);

        // 3. Minimum Spanning Tree / Forest Overlay
        if (showMst && !mstEdges.isEmpty()) {
            drawMstOverlay(gc);
        }

        // 4. Shortest Route Overlay
        if (!showMst && routeNodeIds != null && routeNodeIds.size() > 1) {
            drawRouteOverlay(gc);
        }

        // 5. Selectable Location Markers and Names
        drawLocations(gc);

        // 6. Mandatory OpenStreetMap Attribution Notice
        drawAttributionBadge(gc, w, h);

        // 7. Interactive Zoom Level Indicator Badge
        drawZoomBadge(gc, w, h);
    }

    private void drawRoadNetwork(GraphicsContext gc) {
        Set<String> drawnSegments = new HashSet<>();
        double roadWidth = Math.max(1.5, Math.min(6.0, 2.2 * Math.sqrt(zoomFactor)));
        double pathWidth = Math.max(1.0, Math.min(4.0, 1.4 * Math.sqrt(zoomFactor)));

        for (GraphNode u : graph.getAllNodes().values()) {
            double ux = toPixelX(u.getLongitude());
            double uy = toPixelY(u.getLatitude());

            for (Road road : graph.getNeighbors(u.getId())) {
                long vId = road.getEndNodeId();
                GraphNode v = graph.getNode(vId);
                if (v == null) continue;

                long minId = Math.min(u.getId(), vId);
                long maxId = Math.max(u.getId(), vId);
                String segKey = minId + "_" + maxId;

                if (!drawnSegments.contains(segKey)) {
                    drawnSegments.add(segKey);

                    double vx = toPixelX(v.getLongitude());
                    double vy = toPixelY(v.getLatitude());

                    String type = road.getHighwayType();
                    if ("service".equals(type) || "residential".equals(type) || "living_street".equals(type) || "unclassified".equals(type)) {
                        gc.setStroke(Color.web("#cbd5e1"));
                        gc.setLineWidth(roadWidth);
                    } else if ("crossing".equals(type)) {
                        gc.setStroke(Color.web("#64748b"));
                        gc.setLineWidth(pathWidth);
                    } else {
                        // Dedicated pedestrian paths and steps
                        gc.setStroke(Color.web("#94a3b8"));
                        gc.setLineWidth(pathWidth);
                    }
                    gc.strokeLine(ux, uy, vx, vy);
                }
            }
        }
    }

    private void drawMstOverlay(GraphicsContext gc) {
        double mstWidth = Math.max(3.0, Math.min(8.0, 4.5 * Math.sqrt(zoomFactor)));
        gc.setStroke(Color.web("#16a34a"));
        gc.setLineWidth(mstWidth);

        for (Road edge : mstEdges) {
            GraphNode u = graph.getNode(edge.getStartNodeId());
            GraphNode v = graph.getNode(edge.getEndNodeId());
            if (u != null && v != null) {
                gc.strokeLine(toPixelX(u.getLongitude()), toPixelY(u.getLatitude()),
                        toPixelX(v.getLongitude()), toPixelY(v.getLatitude()));
            }
        }
    }

    private void drawRouteOverlay(GraphicsContext gc) {
        double routeWidth = Math.max(4.0, Math.min(12.0, 5.5 * Math.sqrt(zoomFactor)));
        gc.setStroke(Color.web("#ea580c")); // Orange-red
        gc.setLineWidth(routeWidth);

        for (int i = 0; i < routeNodeIds.size() - 1; i++) {
            GraphNode u = graph.getNode(routeNodeIds.get(i));
            GraphNode v = graph.getNode(routeNodeIds.get(i + 1));
            if (u != null && v != null) {
                gc.strokeLine(toPixelX(u.getLongitude()), toPixelY(u.getLatitude()),
                        toPixelX(v.getLongitude()), toPixelY(v.getLatitude()));
            }
        }

        // Draw route junction step dots
        gc.setFill(Color.web("#c2410c"));
        double dotRadius = Math.max(3.0, Math.min(6.0, 3.5 * Math.sqrt(zoomFactor)));
        for (Long nodeId : routeNodeIds) {
            GraphNode node = graph.getNode(nodeId);
            if (node != null) {
                double nx = toPixelX(node.getLongitude());
                double ny = toPixelY(node.getLatitude());
                gc.fillOval(nx - dotRadius, ny - dotRadius, dotRadius * 2, dotRadius * 2);
            }
        }
    }

    private void drawLocations(GraphicsContext gc) {
        double fontSize = Math.max(10.0, Math.min(14.0, 11.0 * Math.sqrt(zoomFactor)));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, fontSize));

        for (Location loc : graph.getAllLocations()) {
            double x = toPixelX(loc.getLongitude());
            double y = toPixelY(loc.getLatitude());

            // Skip rendering if significantly outside viewport
            if (x < -100 || x > canvas.getWidth() + 100 || y < -100 || y > canvas.getHeight() + 100) {
                continue;
            }

            boolean isStart = selectedStart != null && selectedStart.equals(loc);
            boolean isEnd = selectedDestination != null && selectedDestination.equals(loc);

            if (isStart) {
                gc.setFill(Color.web("#16a34a")); // Green
                gc.fillOval(x - 9, y - 9, 18, 18);
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(2.5);
                gc.strokeOval(x - 9, y - 9, 18, 18);
            } else if (isEnd) {
                gc.setFill(Color.web("#dc2626")); // Red
                gc.fillOval(x - 9, y - 9, 18, 18);
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(2.5);
                gc.strokeOval(x - 9, y - 9, 18, 18);
            } else if (loc.isConnected()) {
                gc.setFill(Color.web("#2563eb")); // Blue
                gc.fillOval(x - 5, y - 5, 10, 10);
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(1.5);
                gc.strokeOval(x - 5, y - 5, 10, 10);
            } else {
                gc.setFill(Color.web("#94a3b8")); // Slate grey for unconnected
                gc.fillOval(x - 4, y - 4, 8, 8);
            }

            // Draw text labels
            if (isStart) {
                gc.setFill(Color.web("#15803d"));
                gc.fillText("START: " + loc.getName(), x + 12, y - 6);
            } else if (isEnd) {
                gc.setFill(Color.web("#b91c1c"));
                gc.fillText("DEST: " + loc.getName(), x + 12, y - 6);
            } else if (zoomFactor >= 1.4 || isProminentLandmark(loc.getName())) {
                gc.setFill(Color.web("#0f172a"));
                gc.fillText(loc.getName(), x + 8, y - 5);
            }
        }
    }

    private void drawZoomBadge(GraphicsContext gc, double w, double h) {
        String zoomText = String.format("Zoom: %.0f%%", zoomFactor * 100);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setTextBaseline(VPos.TOP);

        double badgeWidth = 90.0;
        double badgeHeight = 22.0;
        double badgeX = w - badgeWidth - 10.0;
        double badgeY = 10.0;

        gc.setFill(Color.rgb(255, 255, 255, 0.90));
        gc.fillRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 6, 6);
        gc.setStroke(Color.web("#cbd5e1"));
        gc.setLineWidth(1.0);
        gc.strokeRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 6, 6);

        gc.setFill(Color.web("#334155"));
        gc.fillText(zoomText, w - 18.0, badgeY + 4.0);

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.BASELINE);
    }

    private boolean isProminentLandmark(String name) {
        if (name == null) return false;
        String n = name.toLowerCase();
        return n.contains("library") || n.contains("main gate") || n.contains("food court") ||
                n.contains("tower") || n.contains("auditorium") || n.contains("health") ||
                n.contains("swimming") || n.contains("subway");
    }

    private void drawAttributionBadge(GraphicsContext gc, double w, double h) {
        String attribution = metadata != null ? metadata.getAttributionText() : "Map data © OpenStreetMap contributors";
        gc.setFont(Font.font("Segoe UI", 10));
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setTextBaseline(VPos.BOTTOM);

        double badgeWidth = 240.0;
        double badgeHeight = 22.0;
        double badgeX = w - badgeWidth - 8.0;
        double badgeY = h - badgeHeight - 8.0;

        // Semitransparent white badge background
        gc.setFill(Color.rgb(255, 255, 255, 0.85));
        gc.fillRect(badgeX, badgeY, badgeWidth, badgeHeight);
        gc.setStroke(Color.web("#cbd5e1"));
        gc.setLineWidth(1.0);
        gc.strokeRect(badgeX, badgeY, badgeWidth, badgeHeight);

        // Attribution text
        gc.setFill(Color.web("#475569"));
        gc.fillText(attribution + " (ODbL)", w - 14.0, h - 13.0);

        // Reset alignment
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.BASELINE);
    }

    public void setRoute(List<Long> routeNodeIds) {
        this.routeNodeIds = routeNodeIds != null ? List.copyOf(routeNodeIds) : Collections.emptyList();
        this.showMst = false;
        draw();
    }

    public void setMstEdges(List<Road> mstEdges) {
        this.mstEdges = mstEdges != null ? List.copyOf(mstEdges) : Collections.emptyList();
        this.showMst = true;
        draw();
    }

    public void setSelectedStart(Location selectedStart) {
        this.selectedStart = selectedStart;
        draw();
    }

    public void setSelectedDestination(Location selectedDestination) {
        this.selectedDestination = selectedDestination;
        draw();
    }

    public void clearOverlays() {
        this.routeNodeIds = Collections.emptyList();
        this.mstEdges = Collections.emptyList();
        this.showMst = false;
        this.selectedStart = null;
        this.selectedDestination = null;
        draw();
    }
}

