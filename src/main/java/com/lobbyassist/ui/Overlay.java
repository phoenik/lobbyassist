package com.lobbyassist.ui;

import com.lobbyassist.model.User;
import javafx.beans.InvalidationListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.HashMap;
import java.util.Map;

public class Overlay {

    private final Stage stage = new Stage();
    private final Map<Integer, PingButton> entries = new HashMap<>();

    private class PingButton extends Button {

        private double xoffset = 0;
        private double yoffset = 0;
        private boolean dragging = false;

        private User user;

        public PingButton (Integer id, Long value) {
            super();
            this.autosize();
            this.user = new User(id, value);

            this.setOnMouseClicked(event -> {
                if (event.isShiftDown() && event.isControlDown()) {
                    this.xoffset = event.getSceneX();
                    this.yoffset = event.getSceneY();
                    this.dragging = true;
                } else if(event.isShiftDown()) {
                    this.user.cycle();
                    this.refresh();
                }
            });

            this.setOnMouseDragged(event -> {
                if (dragging) {
                   this.getScene().getWindow().setX(event.getScreenX() - this.xoffset);
                   this.getScene().getWindow().setY(event.getScreenY() - this.yoffset);
                }
            });

            this.setOnMouseReleased(event -> this.dragging = false);

            this.refresh();
        }

        public void update (Long value) {
            this.user.pingProperty().setValue(value);
            this.refresh();
        }

        private void setTextFill(Long value) {
            if ( value >= 180 ) {
                this.setTextFill(Color.RED);
            } else if ( value >= 120 ) {
                this.setTextFill(Color.GOLD);
            } else {
                this.setTextFill(Color.SPRINGGREEN);
            }
        }

        private void refresh () {
            this.setText(String.format("%s: %d",
                    this.user.displayProperty().getValue(),
                    this.user.pingProperty().getValue()));
            this.setTextFill(this.user.pingProperty().getValue());
        }
    }

    public Overlay (ObservableMap<Integer, Long> map) {

        VBox pane = new VBox();
        pane.getStyleClass().add("ping-overlay");
        pane.setMaxHeight(Double.MAX_VALUE);

        for (Map.Entry<Integer, Long> entry : map.entrySet()) {
            PingButton button = new PingButton(entry.getKey(), entry.getValue());
            entries.put(entry.getKey(), button);
            pane.getChildren().add(button);
        }

        map.addListener((MapChangeListener<Integer, Long>)(change -> {
            Integer key = change.getKey();
            Long value = change.getValueAdded();

            if (change.wasAdded() && value != null) {
                if (entries.containsKey(key)) {
                    entries.get(key).update(value);
                } else {
                    PingButton button = new PingButton(key, value);
                    entries.put(key, button);
                    pane.getChildren().add(button);
                }
            }else if(change.wasRemoved()) {
                if (entries.containsKey(key)) {
                    pane.getChildren().remove(entries.get(key));
                    entries.remove(key);
                }
            }

            stage.sizeToScene();
        }));

        pane.autosize();

        Scene scene = new Scene(pane);
        scene.setFill(null);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();
    }

}
