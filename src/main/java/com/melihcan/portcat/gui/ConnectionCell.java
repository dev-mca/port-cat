package com.melihcan.portcat.gui;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import com.melihcan.portcat.model.Connection;

public final class ConnectionCell extends ListCell<Connection> {

    private final HBox root = new HBox(14);

    private final VBox left = new VBox(4);
    private final Label local = new Label();
    private final Label localPort = new Label();

    private final VBox right = new VBox(4);
    private final Label remote = new Label();
    private final Label remotePortState = new Label();

    private final Region spacer = new Region();

    public ConnectionCell() {
        root.getStyleClass().add("conn-card");
        left.getStyleClass().add("conn-block");
        right.getStyleClass().add("conn-block");

        local.getStyleClass().add("conn-title");
        remote.getStyleClass().add("conn-title");
        localPort.getStyleClass().add("conn-meta");
        remotePortState.getStyleClass().add("conn-meta");

        left.getChildren().addAll(local, localPort);
        right.getChildren().addAll(remote, remotePortState);

        HBox.setHgrow(spacer, Priority.ALWAYS);
        root.setAlignment(Pos.CENTER_LEFT);
        root.getChildren().addAll(left, spacer, right);

        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    protected void updateItem(Connection c, boolean empty) {
        super.updateItem(c, empty);

        if (empty || c == null) {
            setGraphic(null);
            getStyleClass().removeAll("state-established", "state-listen", "state-other");
            return;
        }

        local.setText(c.getLocalAddress());
        localPort.setText("Port " + c.getLocalPort());

        remote.setText(c.getRemoteAddress());
        remotePortState.setText("Port " + c.getRemotePort() + " • " + c.getState());

        // State für Hintergrundfarbe
        getStyleClass().removeAll("state-established", "state-listen", "state-other");
        String s = c.getState() == null ? "" : c.getState().toUpperCase();
        if (s.contains("ESTABLISHED")) getStyleClass().add("state-established");
        else if (s.contains("LISTEN")) getStyleClass().add("state-listen");
        else getStyleClass().add("state-other");

        setGraphic(root);
    }
}