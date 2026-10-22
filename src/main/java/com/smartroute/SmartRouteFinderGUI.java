package com.smartroute;

// Explicit JavaFX imports with purpose comments
import javafx.application.Application;                 // JavaFX application lifecycle
import javafx.application.Platform;                    // Running tasks on the JavaFX application thread
import javafx.collections.FXCollections;               // Observable list creation for ComboBox
import javafx.collections.ObservableList;              // Observable list backing ComboBox items
import javafx.collections.transformation.FilteredList; // Dynamically filtered list for ComboBox searching
import javafx.event.EventHandler;                      // Event handler interface for key event interception
import javafx.geometry.Insets;                         // Layout padding and margins
import javafx.geometry.Pos;                            // Layout alignment positioning
import javafx.scene.Cursor;                            // Cursor styling (hand, closed hand for panning)
import javafx.scene.Node;                              // Scene graph node for accessing skin popup content
import javafx.scene.Scene;                             // Primary scene graph container
import javafx.scene.canvas.Canvas;                     // Surface for rendering vector map
import javafx.scene.control.Button;                    // Action buttons (Route, MST, Reset, Zoom)
import javafx.scene.control.ComboBox;                  // Searchable dropdown selection for locations
import javafx.scene.control.Label;                     // Informational and status text labels
import javafx.scene.control.Separator;                 // Visual separator between sidebar panels
import javafx.scene.control.TextField;                 // Text editor inside editable ComboBox
import javafx.scene.control.Tooltip;                   // Tooltip hints for toolbar buttons
import javafx.scene.control.skin.ComboBoxListViewSkin; // Skin access to prevent popup from stealing space key
import javafx.scene.input.KeyCode;                     // KeyCode constants (SPACE, ENTER, etc.)
import javafx.scene.input.KeyEvent;                    // Key event types for keyboard input handling
import javafx.scene.layout.BorderPane;                 // Root container layout
import javafx.scene.layout.HBox;                       // Horizontal layout for map zoom toolbar
import javafx.scene.layout.Priority;                   // Layout resizing priority
import javafx.scene.layout.VBox;                       // Vertical sidebar layout container
import javafx.stage.Stage;                             // Primary application window

// Explicit algorithm and domain model imports
import com.smartroute.algorithm.DijkstraShortestPath;  // Shortest path routing algorithm
import com.smartroute.algorithm.MstResult;             // Spanning tree result container
import com.smartroute.algorithm.PrimMinimumSpanningTree;// Minimum spanning tree algorithm
import com.smartroute.algorithm.RouteLeg;              // Route leg details
import com.smartroute.algorithm.RouteResult;           // Shortest path result container
import com.smartroute.data.CampusDataMetadata;         // Provenance metadata and attribution
import com.smartroute.data.CampusDataRepository;       // Data repository for graph and locations
import com.smartroute.model.CampusGraph;               // Weighted campus graph
import com.smartroute.model.Location;                  // Selectable campus landmark
import com.smartroute.ui.CampusMapRenderer;            // Vector map renderer

// Explicit utility imports
import java.util.ArrayList;                            // Dynamic list for locations and legs
import java.util.List;                                 // List interface for locations and legs
import java.util.function.Consumer;                    // Callback interface for location selection

/**
 * Main JavaFX desktop application for the VIT Vellore Smart Route Finder.
 * <p>
 * This class coordinates the user interface, binds searchable user selections and canvas click
 * events to the decoupled graph repository, triggers Dijkstra's shortest path routing
 * and Prim's minimum spanning tree, provides interactive zoom and pan controls, and delegates
 * map painting to {@link CampusMapRenderer}.
 * </p>
 */
public class SmartRouteFinderGUI extends Application {

    private CampusDataRepository repository;
    private CampusGraph graph;
    private CampusDataMetadata metadata;
    private CampusMapRenderer mapRenderer;

    private Location startLocation;
    private Location destinationLocation;

    private final Canvas mapCanvas = new Canvas(850, 580);
    private final Label statusLabel = new Label("Loading campus map data...");
    private final Label resultLabel = new Label("Select a starting point and destination to find a route.");

    private final ComboBox<String> startBox = new ComboBox<>();
    private final ComboBox<String> endBox = new ComboBox<>();

    // Drag-to-pan tracking coordinates
    private double dragStartX;
    private double dragStartY;
    private double pressStartX;
    private double pressStartY;

    // Flag to suppress combobox text change recursion when setting values programmatically
    private boolean isUpdatingProgrammatically = false;

    // Space key filter to prevent the ComboBox popup ListView from hijacking SPACE as item selection
    private final EventHandler<KeyEvent> spaceKeyFilter = event -> {
        if (event.getCode() == KeyCode.SPACE) {
            event.consume();
        }
    };

    @Override
    public void start(Stage stage) {
        // 1. Initialize data repository
        try {
            this.repository = CampusDataRepository.loadDefault();
            this.graph = repository.getGraph();
            this.metadata = repository.getMetadata();
        } catch (Exception ex) {
            statusLabel.setText("Failed to load campus data: " + ex.getMessage());
            ex.printStackTrace();
        }

        // 2. Prepare location names for searchable combo boxes
        List<String> locationNames = new ArrayList<>();
        if (graph != null) {
            for (Location loc : graph.getAllLocations()) {
                locationNames.add(loc.getName());
            }
            statusLabel.setText("Map loaded: " + graph.getVertexCount() + " vertices, " +
                    (graph.getTotalDirectedEdgeCount() / 2) + " walkways. Ready.");
        }

        // Configure searchable auto-filtering combo boxes
        setupSearchableComboBox(startBox, locationNames, loc -> {
            startLocation = loc;
            if (mapRenderer != null) {
                mapRenderer.setSelectedStart(startLocation);
            }
            if (startLocation != null) {
                statusLabel.setText("Start selected: " + startLocation.getName());
            }
        });

        setupSearchableComboBox(endBox, locationNames, loc -> {
            destinationLocation = loc;
            if (mapRenderer != null) {
                mapRenderer.setSelectedDestination(destinationLocation);
            }
            if (destinationLocation != null) {
                statusLabel.setText("Destination selected: " + destinationLocation.getName());
            }
        });

        startBox.setPromptText("Type or select Start Location...");
        endBox.setPromptText("Type or select Destination...");
        startBox.setMaxWidth(Double.MAX_VALUE);
        endBox.setMaxWidth(Double.MAX_VALUE);

        // 3. Action buttons
        Button routeButton = new Button("Find Shortest Route");
        Button mstButton = new Button("Show Minimum Network");
        Button resetButton = new Button("Reset");

        routeButton.setMaxWidth(Double.MAX_VALUE);
        mstButton.setMaxWidth(Double.MAX_VALUE);
        resetButton.setMaxWidth(Double.MAX_VALUE);

        routeButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-cursor: hand;");
        mstButton.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-cursor: hand;");
        resetButton.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #1e293b; -fx-padding: 8; -fx-cursor: hand;");

        routeButton.setOnAction(e -> findRoute());
        mstButton.setOnAction(e -> showMinimumNetwork());
        resetButton.setOnAction(e -> reset());

        // 4. Build controls sidebar
        VBox controls = new VBox(10,
                new Label("START LOCATION"), startBox,
                new Label("DESTINATION"), endBox,
                routeButton, mstButton, resetButton,
                new Separator(),
                new Label("ROUTE DETAILS"),
                resultLabel,
                new Separator(),
                new Label("HOW TO USE"),
                new Label("1. Search by typing or select via dropdown, or click map markers.\n"
                        + "2. Click 'Find Shortest Route' for Dijkstra shortest path.\n"
                        + "3. Click 'Show Minimum Network' for Prim's MST.\n"
                        + "4. Use toolbar or mouse wheel to Zoom, drag to Pan."),
                new Label("MAP KEY"),
                new Label("Orange Line = Shortest Walking Route\n"
                        + "Green Line = Minimum Spanning Tree\n"
                        + "Blue Circle = Mapped Campus Landmark\n"
                        + "Grey Line = Walkways & Pedestrian Paths")
        );

        controls.setPadding(new Insets(16));
        controls.setPrefWidth(280);
        controls.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-width: 0 1 0 0;");
        resultLabel.setWrapText(true);
        statusLabel.setPadding(new Insets(8, 14, 8, 14));
        statusLabel.setStyle("-fx-background-color: #e2e8f0; -fx-font-size: 11px; -fx-text-fill: #334155;");

        // 5. Setup canvas and vector renderer
        if (graph != null) {
            this.mapRenderer = new CampusMapRenderer(graph, metadata, mapCanvas);
        }

        // 6. Interactive Zoom and Pan Toolbar
        Button zoomInBtn = new Button("➕ Zoom In");
        Button zoomOutBtn = new Button("➖ Zoom Out");
        Button fitBtn = new Button("⟲ Fit to Screen");

        String toolBtnStyle = "-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; "
                + "-fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 10; "
                + "-fx-font-size: 12px; -fx-cursor: hand; -fx-font-weight: bold; -fx-text-fill: #1e293b;";
        zoomInBtn.setStyle(toolBtnStyle);
        zoomOutBtn.setStyle(toolBtnStyle);
        fitBtn.setStyle(toolBtnStyle);

        zoomInBtn.setTooltip(new Tooltip("Zoom In (or use mouse scroll wheel up)"));
        zoomOutBtn.setTooltip(new Tooltip("Zoom Out (or use mouse scroll wheel down)"));
        fitBtn.setTooltip(new Tooltip("Reset zoom and center campus map"));

        zoomInBtn.setOnAction(e -> {
            if (mapRenderer != null) mapRenderer.zoomIn();
        });
        zoomOutBtn.setOnAction(e -> {
            if (mapRenderer != null) mapRenderer.zoomOut();
        });
        fitBtn.setOnAction(e -> {
            if (mapRenderer != null) mapRenderer.resetView();
        });

        Label navHint = new Label("💡 Tip: Scroll wheel zooms • Drag map to pan • Click landmark to select");
        navHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        HBox zoomToolbar = new HBox(8, zoomInBtn, zoomOutBtn, fitBtn, new Separator(), navHint);
        zoomToolbar.setAlignment(Pos.CENTER_LEFT);
        zoomToolbar.setPadding(new Insets(6, 10, 6, 10));
        zoomToolbar.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

        // 7. Interactive Canvas Listeners (Mouse Wheel Zoom, Mouse Drag Pan, Click Selection)
        mapCanvas.setOnScroll(e -> {
            if (mapRenderer != null) {
                double factor = (e.getDeltaY() > 0) ? 1.15 : 0.87;
                mapRenderer.zoomAt(e.getX(), e.getY(), factor);
            }
        });

        mapCanvas.setOnMousePressed(e -> {
            dragStartX = e.getX();
            dragStartY = e.getY();
            pressStartX = e.getX();
            pressStartY = e.getY();
            mapCanvas.setCursor(Cursor.CLOSED_HAND);
        });

        mapCanvas.setOnMouseDragged(e -> {
            if (mapRenderer != null) {
                double dx = e.getX() - dragStartX;
                double dy = e.getY() - dragStartY;
                mapRenderer.pan(dx, dy);
                dragStartX = e.getX();
                dragStartY = e.getY();
            }
        });

        mapCanvas.setOnMouseReleased(e -> {
            mapCanvas.setCursor(Cursor.HAND);
            double dragDistance = Math.hypot(e.getX() - pressStartX, e.getY() - pressStartY);
            // Only trigger location selection if the mouse was clicked, not dragged to pan
            if (dragDistance < 6.0) {
                selectLocationByClick(e.getX(), e.getY());
            }
        });

        mapCanvas.setOnMouseMoved(e -> {
            mapCanvas.setCursor(Cursor.HAND);
        });

        // 8. Responsive map pane container
        BorderPane mapPane = new BorderPane();
        mapPane.setTop(zoomToolbar);
        mapPane.setCenter(mapCanvas);
        mapPane.setStyle("-fx-background-color: #f8fafc;");
        BorderPane.setMargin(mapCanvas, new Insets(6));

        mapPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            double w = newVal.doubleValue() - 16;
            if (w > 100) {
                mapCanvas.setWidth(w);
                if (mapRenderer != null) mapRenderer.draw();
            }
        });
        mapPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            double h = newVal.doubleValue() - 55;
            if (h > 100) {
                mapCanvas.setHeight(h);
                if (mapRenderer != null) mapRenderer.draw();
            }
        });

        BorderPane root = new BorderPane();
        root.setLeft(controls);
        root.setCenter(mapPane);
        root.setBottom(statusLabel);

        Scene scene = new Scene(root, 1200, 740);
        stage.setTitle("Smart Route Finder — VIT Vellore Campus (PAMCA501 DSA)");
        stage.setScene(scene);
        stage.show();

        if (mapRenderer != null) {
            mapRenderer.draw();
        }
    }

    /**
     * Configures a ComboBox to allow typing to dynamically search and filter through campus landmarks.
     *
     * @param comboBox           the target ComboBox to configure
     * @param allItems           the master list of landmark names
     * @param onLocationSelected callback triggered when a location is chosen or committed
     */
    private void setupSearchableComboBox(ComboBox<String> comboBox, List<String> allItems,
                                         Consumer<Location> onLocationSelected) {
        comboBox.setEditable(true);
        ObservableList<String> masterItems = FXCollections.observableArrayList(allItems);
        FilteredList<String> filteredItems = new FilteredList<>(masterItems, p -> true);
        comboBox.setItems(filteredItems);

        TextField editor = comboBox.getEditor();

        // Dynamically filter items as user types
        editor.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdatingProgrammatically) return;

            String selected = comboBox.getSelectionModel().getSelectedItem();
            if (selected != null && selected.equals(editor.getText())) {
                return;
            }

            Platform.runLater(() -> {
                if (isUpdatingProgrammatically) return;

                String query = editor.getText();
                if (query == null || query.trim().isEmpty()) {
                    filteredItems.setPredicate(s -> true);
                } else {
                    String[] terms = query.trim().toLowerCase().split("\\s+");
                    filteredItems.setPredicate(name -> {
                        String lowerName = name.toLowerCase();
                        for (String term : terms) {
                            if (!lowerName.contains(term)) {
                                return false;
                            }
                        }
                        return true;
                    });
                }

                if (!comboBox.isShowing() && comboBox.isFocused() && !filteredItems.isEmpty()) {
                    comboBox.show();
                }
            });
        });

        // Attach space key filter to popup ListView so typing space is never hijacked as item selection
        attachSpaceFilter(comboBox);
        comboBox.skinProperty().addListener((obs, oldSkin, newSkin) -> attachSpaceFilter(comboBox));
        comboBox.showingProperty().addListener((obs, wasShowing, isNowShowing) -> {
            if (isNowShowing) {
                attachSpaceFilter(comboBox);
            }
        });

        // When user selects an item from the dropdown popup
        comboBox.setOnAction(e -> {
            if (isUpdatingProgrammatically) return;
            String val = comboBox.getValue();
            if (val != null && graph != null) {
                Location loc = graph.getLocationByName(val);
                if (loc != null) {
                    onLocationSelected.accept(loc);
                }
            }
        });

        // When focus is lost or Enter is pressed, commit the best match
        editor.setOnAction(e -> commitSelection(comboBox, onLocationSelected));
        editor.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                commitSelection(comboBox, onLocationSelected);
            }
        });
    }

    /**
     * Attaches an event filter to the ComboBox popup ListView to prevent it from intercepting
     * the SPACE key as an item selection / activation shortcut, allowing multi-word typing.
     *
     * @param comboBox the ComboBox whose popup skin content to protect
     */
    private void attachSpaceFilter(ComboBox<String> comboBox) {
        if (comboBox.getSkin() instanceof ComboBoxListViewSkin<?> skin) {
            Node popup = skin.getPopupContent();
            if (popup != null) {
                popup.removeEventFilter(KeyEvent.KEY_PRESSED, spaceKeyFilter);
                popup.addEventFilter(KeyEvent.KEY_PRESSED, spaceKeyFilter);
            }
        }
    }

    /**
     * Commits the text in the ComboBox editor to the best matching registered campus location.
     */
    private void commitSelection(ComboBox<String> comboBox, Consumer<Location> onLocationSelected) {
        if (graph == null) return;
        String text = comboBox.getEditor().getText();
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        // 1. Try exact match
        Location loc = graph.getLocationByName(text);

        // 2. Try prefix/contains search
        if (loc == null) {
            List<Location> matches = graph.searchLocationsByName(text);
            if (!matches.isEmpty()) {
                loc = matches.get(0);
            }
        }

        if (loc != null) {
            isUpdatingProgrammatically = true;
            try {
                comboBox.setValue(loc.getName());
                comboBox.getEditor().setText(loc.getName());
            } finally {
                isUpdatingProgrammatically = false;
            }
            onLocationSelected.accept(loc);
        }
    }

    private void selectLocationByClick(double clickX, double clickY) {
        if (mapRenderer == null || graph == null) return;

        Location nearest = mapRenderer.findNearestLocation(clickX, clickY, 25.0);
        if (nearest == null) return;

        if (startLocation == null || destinationLocation != null) {
            // Set new start
            startLocation = nearest;
            destinationLocation = null;

            isUpdatingProgrammatically = true;
            try {
                startBox.setValue(startLocation.getName());
                startBox.getEditor().setText(startLocation.getName());
                endBox.setValue(null);
                endBox.getEditor().clear();
            } finally {
                isUpdatingProgrammatically = false;
            }

            mapRenderer.clearOverlays();
            mapRenderer.setSelectedStart(startLocation);
            mapRenderer.setSelectedDestination(null);

            statusLabel.setText("Start selected: " + startLocation.getName() + ". Now select a destination.");
            resultLabel.setText("Selected start: " + startLocation.getName());
        } else {
            // Set destination
            destinationLocation = nearest;

            isUpdatingProgrammatically = true;
            try {
                endBox.setValue(destinationLocation.getName());
                endBox.getEditor().setText(destinationLocation.getName());
            } finally {
                isUpdatingProgrammatically = false;
            }

            mapRenderer.setSelectedDestination(destinationLocation);
            statusLabel.setText("Destination selected: " + destinationLocation.getName() + ". Click Find Shortest Route.");
            resultLabel.setText("From: " + startLocation.getName() + "\nTo: " + destinationLocation.getName());
        }
    }

    private void findRoute() {
        // Auto-commit typed values if user clicked Find Route without pressing Enter
        if (startLocation == null && !startBox.getEditor().getText().trim().isEmpty()) {
            commitSelection(startBox, loc -> startLocation = loc);
        }
        if (destinationLocation == null && !endBox.getEditor().getText().trim().isEmpty()) {
            commitSelection(endBox, loc -> destinationLocation = loc);
        }

        if (startLocation == null || destinationLocation == null) {
            statusLabel.setText("Please select both a start location and destination.");
            resultLabel.setText("Selection incomplete. Please select or search both locations.");
            return;
        }

        if (!startLocation.isConnected()) {
            statusLabel.setText("Start landmark '" + startLocation.getName() + "' is not connected to a campus walkway.");
            resultLabel.setText("Cannot route: '" + startLocation.getName() + "' is not attached to a walkable path. Please choose a mapped campus location (blue markers).");
            return;
        }

        if (!destinationLocation.isConnected()) {
            statusLabel.setText("Destination landmark '" + destinationLocation.getName() + "' is not connected to a campus walkway.");
            resultLabel.setText("Cannot route: '" + destinationLocation.getName() + "' is not attached to a walkable path. Please choose a mapped campus location (blue markers).");
            return;
        }

        DijkstraShortestPath router = new DijkstraShortestPath(graph);
        RouteResult result = router.findShortestPath(startLocation, destinationLocation);

        if (result.isSuccess()) {
            mapRenderer.setRoute(result.getPathNodeIds());
            mapRenderer.setSelectedStart(startLocation);
            mapRenderer.setSelectedDestination(destinationLocation);

            StringBuilder sb = new StringBuilder();
            sb.append("Shortest Route Found!\n");
            sb.append("Total Distance: ").append(String.format("%.1f", result.getTotalDistanceMeters())).append(" m\n");
            sb.append("Walking Segments: ").append(result.getHopCount()).append("\n\n");

            sb.append("Route Steps:\n");
            int stepNum = 1;
            for (RouteLeg leg : result.getLegs()) {
                sb.append(stepNum++).append(". ").append(String.format("%.1f", leg.getDistanceMeters()))
                        .append("m along ").append(leg.getWayName()).append("\n");
            }

            resultLabel.setText(sb.toString());
            statusLabel.setText("Route calculated via Dijkstra algorithm (" + String.format("%.1f", result.getTotalDistanceMeters()) + " m).");
        } else {
            mapRenderer.clearOverlays();
            mapRenderer.setSelectedStart(startLocation);
            mapRenderer.setSelectedDestination(destinationLocation);
            resultLabel.setText("Route Status: " + result.getStatus() + "\n" + result.getMessage()
                    + "\n\nNote: Both landmarks exist in OpenStreetMap, but no continuous walkable path connects them in the campus dataset.");
            statusLabel.setText(result.getMessage());
        }
    }

    private void showMinimumNetwork() {
        if (graph == null) return;

        PrimMinimumSpanningTree prim = new PrimMinimumSpanningTree(graph);
        MstResult mst = prim.findMinimumSpanningForest();

        mapRenderer.setMstEdges(mst.getEdges());

        resultLabel.setText("Minimum Spanning " + (mst.isForest() ? "Forest" : "Tree") + "\n"
                + "Algorithm: Prim's Algorithm\n"
                + "Walkway Segments: " + mst.getEdgeCount() + "\n"
                + "Vertices Spanned: " + mst.getSpannedVertexCount() + "\n"
                + "Total Network Length: " + String.format("%.1f", mst.getTotalWeightMeters()) + " m\n\n"
                + "Note: An MST represents the minimum connected infrastructure, not a point-to-point shortest route.");

        statusLabel.setText("Green overlay shows Prim's minimum spanning network (" + String.format("%.1f", mst.getTotalWeightMeters()) + " m).");
    }

    private void reset() {
        startLocation = null;
        destinationLocation = null;
        isUpdatingProgrammatically = true;
        try {
            startBox.setValue(null);
            startBox.getEditor().clear();
            endBox.setValue(null);
            endBox.getEditor().clear();
        } finally {
            isUpdatingProgrammatically = false;
        }
        if (mapRenderer != null) {
            mapRenderer.clearOverlays();
            mapRenderer.resetView();
        }
        resultLabel.setText("Select a starting point and destination to find a route.");
        statusLabel.setText("Selections and view cleared. Ready.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
