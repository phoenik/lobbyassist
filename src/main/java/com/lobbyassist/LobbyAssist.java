package com.lobbyassist;

import com.lobbyassist.model.User;
import com.lobbyassist.net.packet.CaptureTask;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.pcap4j.core.PcapAddress;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class LobbyAssist extends Application {

    private ObservableMap<Integer, User> users = FXCollections.observableHashMap();
    private ObservableMap<Integer, Long> pings = FXCollections.observableHashMap();
    private CaptureTask<Void> capture;
    private Stage stage;

    private double x = 0;
    private double y = 0;
    private boolean dragging = false;

    private class UserButton extends Button {

        private User user;

        UserButton (User user) {
            this.user = user;
            this.setPing(this.user.getPing());
            this.setMaxWidth(Double.POSITIVE_INFINITY);
            this.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                   this.user.cycle();
                   this.setPing(this.user.getPing());
                }
            });
        }

        public void setPing (Long ping) {
            this.user.setPing(ping);
            this.setText(this.user.toString());

            if(ping >= 200) {
                this.setTextFill(Color.RED);
            } else if(ping > 100) {
                this.setTextFill(Color.GOLD);
            } else {
                this.setTextFill(Color.LIGHTGREEN);
            }

        }

    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        BorderPane pane = new BorderPane();
        pane.getStyleClass().add("la-main");

        pane.setTop(menu());

        pane.setCenter(list());
        pane.autosize();

        Scene scene = new Scene(pane);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.setResizable(false);

        primaryStage.show();

        scene.getWindow().focusedProperty().addListener((c, old, show) -> {
            pane.getTop().visibleProperty().setValue(show);
            primaryStage.setWidth(show ? 225 : 100);
        });
    }

    private Node menu () {
        HBox menu = new HBox();
        menu.getStyleClass().add("la-menu");
        menu.setMaxWidth(Double.POSITIVE_INFINITY);

        ComboBox<InetAddress> interfaces = new ComboBox<>();
        Button exit = new Button("\u2715");
        Button drag = new Button("\u2261");
        drag.getStyleClass().add("drag-button");
        drag.setOnMousePressed(event -> {
            x = event.getSceneX();
            y = event.getSceneY();
        });
        drag.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - x);
            stage.setY(event.getScreenY() - y);
        });

        exit.setOnMouseClicked(e -> Platform.exit());

        interfaces.setMaxWidth(Double.POSITIVE_INFINITY);
        interfaces.setEditable(false);

        try {
            for (PcapNetworkInterface pni : Pcaps.findAllDevs()) {
                for (PcapAddress address : pni.getAddresses()) {
                    if (address.getNetmask() != null) {
                        interfaces.getItems().add(address.getAddress());
                    }
                }
            }
        } catch (PcapNativeException e) {
            e.printStackTrace();
        }

        interfaces.setOnAction(e -> {
            InetAddress selected = interfaces.getValue();

            if (selected != null) {
                if (capture != null) capture.cancel();
                capture = new CaptureTask<>(selected, pings);
                Thread thread = new Thread(capture);
                thread.setDaemon(true);
                thread.start();
            }
        });

        HBox.setHgrow(interfaces, Priority.ALWAYS);

        menu.getChildren().addAll(drag, interfaces, exit);
        return menu;
    }

    private Node list () {
        VBox pane = new VBox(1);
        pane.getStyleClass().add("la-pings");
        pane.autosize();
        Map<Integer, UserButton> buttons = new HashMap<>();

        pane.getChildren().add(new UserButton(new User(0, 1)));
        pane.getChildren().add(new UserButton(new User(1, 101)));
        pane.getChildren().add(new UserButton(new User(2, 201)));

        pings.addListener((MapChangeListener<Integer, Long>) change -> {
            final Integer key = change.getKey();
            final Long value = change.getValueAdded();

            if (change.wasAdded()) {
                if (users.containsKey(key) && buttons.containsKey(key)) {
                    buttons.get(key).setPing(value);
                } else if (users.containsKey(key)) {
                    User user = users.get(key);
                    UserButton button = new UserButton(user);
                    button.setPing(value);
                    buttons.put(key, button);
                } else {
                    User user = new User(key, value);
                    UserButton button = new UserButton(user);
                    users.put(key, new User(key, value));
                    buttons.put(key, button);
                    pane.getChildren().add(button);
                }
            } else if (change.wasRemoved()) {
                UserButton button = buttons.get(key);
                button.setPing(-1L);
                pane.getChildren().remove(button);
                buttons.remove(key);
            }

            stage.sizeToScene();
        });

        return pane;
    }
}
