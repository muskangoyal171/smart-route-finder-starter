@echo off
set JFX_PATH=%USERPROFILE%\.m2\repository\org\openjfx\javafx-base\21.0.8\javafx-base-21.0.8-win.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-graphics\21.0.8\javafx-graphics-21.0.8-win.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-controls\21.0.8\javafx-controls-21.0.8-win.jar

echo Starting Smart Route Finder - VIT Vellore Campus...
java --module-path "%JFX_PATH%" --add-modules javafx.controls -cp "target/classes" com.smartroute.SmartRouteFinderGUI
