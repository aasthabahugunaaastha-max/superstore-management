// src/main/java/com/superstore/ui/LoginView.java
package com.superstore.ui;

import com.superstore.core.SuperstoreSystem;
import com.superstore.user.EndUser;
import com.superstore.user.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class LoginView {

    private final BorderPane root;
    private final MainApp app;
    private final SuperstoreSystem system;

    public LoginView(MainApp app, SuperstoreSystem system) {
        this.app = app;
        this.system = system;
        this.root = new BorderPane();
        build();
    }

    private void build() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Label userLabel = new Label("Username:");
        TextField usernameField = new TextField();

        Label passLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Label typeLabel = new Label("User Type:");
        ComboBox<User.UserType> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(User.UserType.values());
        typeBox.setValue(User.UserType.SUPER_USER);

        Button loginBtn = new Button("Login");
        Button guestBtn = new Button("Guest (End User)");

        Label status = new Label();

        grid.add(userLabel, 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(typeLabel, 0, 2);
        grid.add(typeBox, 1, 2);

        HBox buttons = new HBox(10, loginBtn, guestBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        grid.add(buttons, 1, 3);
        grid.add(status, 1, 4);

        loginBtn.setOnAction(e -> {
            String uname = usernameField.getText();
            String pwd = passwordField.getText();
            User user = system.authenticateUser(uname, pwd);
            if (user == null || user.getUserType() != typeBox.getValue()) {
                status.setText("Invalid credentials or user type.");
                return;
            }
            app.showDashboard(user);
        });

        guestBtn.setOnAction(e -> {
            User guest = new EndUser();
            app.showDashboard(guest);
        });

        root.setCenter(grid);
    }

    public BorderPane getRoot() {
        return root;
    }
}
