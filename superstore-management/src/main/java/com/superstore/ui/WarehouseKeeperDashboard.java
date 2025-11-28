package com.superstore.ui;

import com.superstore.core.SuperstoreSystem;
import com.superstore.domain.Warehouse;
import com.superstore.model.Category;
import com.superstore.model.Item;
import com.superstore.model.Subcategory;
import com.superstore.user.WarehouseKeeper;
import javafx.beans.property.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;


public class WarehouseKeeperDashboard {

    private final BorderPane root;
    private final SuperstoreSystem system;
    private final WarehouseKeeper keeper;
    private final Warehouse warehouse;
    private final MainApp app;

    private final ObservableList<Category> categoriesObs = FXCollections.observableArrayList();
    private final ObservableList<Subcategory> subcategoriesObs = FXCollections.observableArrayList();
    private final ObservableList<Item> itemsObs = FXCollections.observableArrayList();

    public WarehouseKeeperDashboard(MainApp app, SuperstoreSystem system, WarehouseKeeper keeper) {
        this.app = app;
        this.system = system;
        this.keeper = keeper;
        this.warehouse = system.getWarehouse(keeper.getFacilityId());
        this.root = new BorderPane();
        build();
        refreshData();
    }

    public BorderPane getRoot() {
        return root;
    }

    private void build() {
        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> app.showLoginView());

        HBox topBar = new HBox(10,
                new Label("Warehouse Keeper: " + keeper.getUsername()
                        + " | Warehouse: " + warehouse.getWarehouseName()),
                logoutBtn);
        topBar.setPadding(new Insets(5));

        VBox left = new VBox(10);
        left.setPadding(new Insets(10));

        ComboBox<Category> categoryBox = new ComboBox<>(categoriesObs);
        categoryBox.setPromptText("Category");

        ComboBox<Subcategory> subcategoryBox = new ComboBox<>(subcategoriesObs);
        subcategoryBox.setPromptText("Subcategory");

        categoryBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCategoryName());
            }
        });
        categoryBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getCategoryName());
            }
        });

        subcategoryBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Subcategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getSubcategoryName());
            }
        });
        subcategoryBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Subcategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getSubcategoryName());
            }
        });

        categoryBox.valueProperty().addListener((obs, o, n) -> {
            subcategoriesObs.clear();
            itemsObs.clear();
            if (n != null) {
                subcategoriesObs.addAll(n.getAllSubcategories());
            }
        });

        subcategoryBox.valueProperty().addListener((obs, o, n) -> {
            itemsObs.clear();
            if (n != null) {
                itemsObs.addAll(n.getAllItems());
            }
        });

        left.getChildren().addAll(
                new Label("Select Category/Subcategory"),
                categoryBox,
                subcategoryBox
        );

        TableView<Item> table = new TableView<>(itemsObs);

        TableColumn<Item, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getItemCode()));

        TableColumn<Item, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getItemName()));

        TableColumn<Item, Number> invCol = new TableColumn<>("Inventory");
        invCol.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getCurrentInventoryLevel()));

        table.getColumns().addAll(codeCol, nameCol, invCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TextField qtyField = new TextField();
        qtyField.setPromptText("Qty");
        Button inBtn = new Button("Inventory In");
        Button outBtn = new Button("Inventory Out");

        inBtn.setOnAction(e -> {
            Item sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            int q = parseQty(qtyField.getText());
            if (q <= 0) return;
            sel.addInventory(q);
            table.refresh();
        });

        outBtn.setOnAction(e -> {
            Item sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            int q = parseQty(qtyField.getText());
            if (q <= 0) return;
            sel.removeInventory(q);
            if (sel.getCurrentInventoryLevel() < 0) sel.setCurrentInventoryLevel(0);
            table.refresh();
        });

        HBox invBox = new HBox(5, new Label("Qty:"), qtyField, inBtn, outBtn);
        invBox.setAlignment(Pos.CENTER_RIGHT);

        VBox center = new VBox(10,
                new Label("Warehouse Items"),
                table,
                invBox);
        center.setPadding(new Insets(10));

        SplitPane split = new SplitPane(left, center);
        split.setDividerPositions(0.25);

        root.setTop(topBar);
        root.setCenter(split);
    }

    private int parseQty(String txt) {
        try {
            return Integer.parseInt(txt);
        } catch (NumberFormatException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Quantity must be a positive integer");
            a.showAndWait();
            return -1;
        }
    }

    private void refreshData() {
        categoriesObs.setAll(warehouse.getAllCategories());
        itemsObs.clear();
        for (Category c : warehouse.getAllCategories()) {
            for (Subcategory s : c.getAllSubcategories()) {
                itemsObs.addAll(s.getAllItems());
            }
        }
    }
}

