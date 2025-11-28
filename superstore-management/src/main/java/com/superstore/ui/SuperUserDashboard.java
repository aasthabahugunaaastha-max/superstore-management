package com.superstore.ui;

import com.superstore.core.SuperstoreSystem;
import com.superstore.domain.Store;
import com.superstore.domain.Warehouse;
import com.superstore.user.SuperUser;
import com.superstore.user.User;
import com.superstore.user.WarehouseAdministrator;
import com.superstore.user.StoreAdministrator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SuperUserDashboard {

    private final BorderPane root;
    private final SuperstoreSystem system;
    private final SuperUser superUser;
    private final MainApp app;

    private final ObservableList<Warehouse> warehousesObs;
    private final ObservableList<Store> storesObs;
    private final ObservableList<String> warehouseAdminsObs = FXCollections.observableArrayList();
    private final ObservableList<String> storeAdminsObs = FXCollections.observableArrayList();

    private ListView<String> warehouseAdminsList;
    private ListView<String> storeAdminsList;
    private ComboBox<Warehouse> warehouseSelectBox;
    private ComboBox<Store> storeSelectBox;

    public SuperUserDashboard(MainApp app, SuperstoreSystem system, SuperUser superUser) {
        this.app = app;
        this.system = system;
        this.superUser = superUser;
        this.root = new BorderPane();
        this.warehousesObs = FXCollections.observableArrayList(system.getAllWarehouses());
        this.storesObs = FXCollections.observableArrayList(system.getAllStores());
        build();
        refreshAllData();
    }

    private void build() {
        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> app.showLoginView());

        HBox topBar = new HBox(10,
                new Label("Logged in as Super User: " + superUser.getUsername()),
                logoutBtn);
        topBar.setPadding(new Insets(5));

        SplitPane split = new SplitPane();
        split.getItems().addAll(createWarehousePane(), createStorePane());
        split.setDividerPositions(0.5);

        root.setTop(topBar);
        root.setCenter(split);
    }

    private VBox createWarehousePane() {
        VBox mainBox = new VBox(10);
        mainBox.setPadding(new Insets(10));

        TableView<Warehouse> table = new TableView<>(warehousesObs);
        TableColumn<Warehouse, String> idCol = new TableColumn<>("Warehouse ID");
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWarehouseId()));
        TableColumn<Warehouse, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWarehouseName()));
        table.getColumns().addAll(idCol, nameCol);

        TextField idField = new TextField();
        idField.setPromptText("ID");
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        Button addBtn = new Button("Add Warehouse");
        addBtn.setOnAction(e -> {
            String id = idField.getText().trim();
            String nm = nameField.getText().trim();
            if (id.isEmpty() || nm.isEmpty())
                return;
            system.createWarehouse(id, nm);
            warehousesObs.setAll(system.getAllWarehouses());
            idField.clear();
            nameField.clear();
        });

        Label adminLabel = new Label("Warehouse Admins");
        warehouseAdminsList = new ListView<>(warehouseAdminsObs);
        warehouseAdminsList.setPrefHeight(120);

        warehouseSelectBox = new ComboBox<>(warehousesObs);
        warehouseSelectBox.setPromptText("Select Warehouse");
        setupComboBoxDisplay(warehouseSelectBox, Warehouse::getWarehouseName);

        HBox adminButtons = new HBox(5,
            createSmallButton("➕Add", this::addWarehouseAdmin),
            createSmallButton("✏️Edit", this::editWarehouseAdmin),
            createSmallButton("🗑️Delete", this::deleteWarehouseAdmin)
        );

        VBox adminSection = new VBox(6, adminLabel, warehouseSelectBox, warehouseAdminsList, adminButtons);

        mainBox.getChildren().addAll(
                new Label("Warehouses"),
                table,
                new HBox(5, idField, nameField, addBtn),
                new Separator(),
                adminSection
        );
        return mainBox;
    }

    private VBox createStorePane() {
        VBox mainBox = new VBox(10);
        mainBox.setPadding(new Insets(10));

        TableView<Store> table = new TableView<>(storesObs);
        TableColumn<Store, String> idCol = new TableColumn<>("Store ID");
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStoreId()));
        TableColumn<Store, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStoreName()));
        TableColumn<Store, String> whCol = new TableColumn<>("Warehouse");
        whCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLinkedWarehouseId()));
        table.getColumns().addAll(idCol, nameCol, whCol);

        TextField idField = new TextField();
        idField.setPromptText("ID");
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField whField = new TextField();
        whField.setPromptText("Warehouse ID");
        Button addBtn = new Button("Add Store");
        addBtn.setOnAction(e -> {
            String id = idField.getText().trim();
            String nm = nameField.getText().trim();
            String wh = whField.getText().trim();
            if (id.isEmpty() || nm.isEmpty() || wh.isEmpty())
                return;
            system.createStore(id, nm, wh);
            storesObs.setAll(system.getAllStores());
            idField.clear();
            nameField.clear();
            whField.clear();
        });

        Label adminLabel = new Label("Store Admins");
        storeAdminsList = new ListView<>(storeAdminsObs);
        storeAdminsList.setPrefHeight(120);

        storeSelectBox = new ComboBox<>(storesObs);
        storeSelectBox.setPromptText("Select Store");
        setupComboBoxDisplay(storeSelectBox, Store::getStoreName);

        HBox adminButtons = new HBox(5,
            createSmallButton("➕Add", this::addStoreAdmin),
            createSmallButton("✏️Edit", this::editStoreAdmin),
            createSmallButton("🗑️Delete", this::deleteStoreAdmin)
        );

        VBox adminSection = new VBox(6, adminLabel, storeSelectBox, storeAdminsList, adminButtons);

        mainBox.getChildren().addAll(
            new Label("Stores"),
            table,
            new HBox(5, idField, nameField, whField, addBtn),
            new Separator(),
            adminSection
        );
        return mainBox;
    }

    private <T> void setupComboBoxDisplay(ComboBox<T> comboBox, java.util.function.Function<T, String> nameGetter) {
        comboBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : nameGetter.apply(item));
            }
        });
        comboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : nameGetter.apply(item));
            }
        });
    }

    // ✅ FIXED: UNIQUE USERNAME with "wa_" prefix
    private void addWarehouseAdmin() {
        Warehouse selected = warehouseSelectBox.getValue();
        if (selected == null) return;
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Add Warehouse Admin for " + selected.getWarehouseName());
        dialog.setContentText("Base username (will be: wa_username):");
        dialog.showAndWait().ifPresent(baseUsername -> {
            if (!baseUsername.trim().isEmpty()) {
                String uniqueUsername = "wa_" + baseUsername.trim();
                WarehouseAdministrator admin = new WarehouseAdministrator(
                    "WA-" + UUID.randomUUID(), uniqueUsername, "password", selected.getWarehouseId());
                system.createUser(admin);
                refreshAdminData();
            }
        });
    }

    private void editWarehouseAdmin() {
        // Optional edit implementation
    }

    private void deleteWarehouseAdmin() {
        String selected = warehouseAdminsList.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String username = selected.split(" \\(")[0].trim();

        List<User> admins = system.getAllUsers().stream()
                .filter(u -> u instanceof WarehouseAdministrator)
                .collect(Collectors.toList());

        for (User u : admins) {
            if (u.getUsername().equals(username)) {
                system.deleteUser(u);
                refreshAdminData();
                return;
            }
        }
    }

    // ✅ FIXED: UNIQUE USERNAME with "sa_" prefix
    private void addStoreAdmin() {
        Store selected = storeSelectBox.getValue();
        if (selected == null) return;
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Add Store Admin for " + selected.getStoreName());
        dialog.setContentText("Base username (will be: sa_username):");
        dialog.showAndWait().ifPresent(baseUsername -> {
            if (!baseUsername.trim().isEmpty()) {
                String uniqueUsername = "sa_" + baseUsername.trim();
                StoreAdministrator admin = new StoreAdministrator(
                    "SA-" + UUID.randomUUID(), uniqueUsername, "password", selected.getStoreId());
                system.createUser(admin);
                refreshAdminData();
            }
        });
    }

    private void editStoreAdmin() {
        // Optional edit implementation
    }

    private void deleteStoreAdmin() {
        String selected = storeAdminsList.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String username = selected.split(" \\(")[0].trim();

        List<User> admins = system.getAllUsers().stream()
                .filter(u -> u instanceof StoreAdministrator)
                .collect(Collectors.toList());

        for (User u : admins) {
            if (u.getUsername().equals(username)) {
                system.deleteUser(u);
                refreshAdminData();
                return;
            }
        }
    }

    private void refreshAllData() {
        warehousesObs.setAll(system.getAllWarehouses());
        storesObs.setAll(system.getAllStores());
        refreshAdminData();
    }

    private void refreshAdminData() {
        warehouseAdminsObs.clear();
        storeAdminsObs.clear();

        List<User> allUsers = system.getAllUsers();

        for (User user : allUsers) {
            if (user instanceof WarehouseAdministrator) {
                WarehouseAdministrator wa = (WarehouseAdministrator) user;
                Warehouse wh = warehousesObs.stream()
                        .filter(w -> w.getWarehouseId().equals(wa.getFacilityId()))
                        .findFirst().orElse(null);
                String display = wa.getUsername() + (wh != null ? " (" + wh.getWarehouseName() + ")" : " (Unknown)");
                warehouseAdminsObs.add(display);
            } else if (user instanceof StoreAdministrator) {
                StoreAdministrator sa = (StoreAdministrator) user;
                Store st = storesObs.stream()
                        .filter(s -> s.getStoreId().equals(sa.getFacilityId()))
                        .findFirst().orElse(null);
                String display = sa.getUsername() + (st != null ? " (" + st.getStoreName() + ")" : " (Unknown)");
                storeAdminsObs.add(display);
            }
        }
    }

    private Button createSmallButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setOnAction(e -> action.run());
        btn.setMinWidth(60);
        btn.setStyle("-fx-font-size: 10px;");
        return btn;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.showAndWait();
    }

    public BorderPane getRoot() {
        return root;
    }
}

