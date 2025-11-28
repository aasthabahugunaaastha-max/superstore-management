// src/main/java/com/superstore/ui/EndUserDashboard.java
package com.superstore.ui;

import com.superstore.core.SuperstoreSystem;
import com.superstore.domain.Store;
import com.superstore.model.Category;
import com.superstore.model.Item;
import com.superstore.model.Subcategory;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EndUserDashboard {

    private final BorderPane root = new BorderPane();
    private final SuperstoreSystem system;
    private final MainApp app;


    private final ObservableList<Store> storesObs = FXCollections.observableArrayList();
    private final ObservableList<Category> categoriesObs = FXCollections.observableArrayList();
    private final ObservableList<Subcategory> subcategoriesObs = FXCollections.observableArrayList();
    private final ObservableList<Item> itemsObs = FXCollections.observableArrayList();

   public EndUserDashboard(MainApp app, SuperstoreSystem system) {
	    this.app = app;
	    this.system = system;
	    build();
	    refreshStores();
	}
    public BorderPane getRoot() {
        return root;
    }

    private void build() {
        Label header = new Label("End User (Guest) – Browse Superstore");
        header.setPadding(new Insets(5));
        root.setTop(header);

        // Store selector
        ComboBox<Store> storeBox = new ComboBox<>(storesObs);
        storeBox.setPromptText("Select Store");
        storeBox.setMinWidth(250);

        storeBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Store item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getStoreName());
            }
        });
        storeBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Store item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getStoreName());
            }
        });

        // Category / subcategory selectors
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

        // Wiring selection logic: store → categories, category → subcategories, subcategory → items
        storeBox.valueProperty().addListener((obs, oldV, newStore) -> {
            categoriesObs.clear();
            subcategoriesObs.clear();
            itemsObs.clear();
            if (newStore != null) {
                categoriesObs.addAll(newStore.getAllCategories());
            }
        });

        categoryBox.valueProperty().addListener((obs, oldV, newCat) -> {
            subcategoriesObs.clear();
            itemsObs.clear();
            if (newCat != null) {
                subcategoriesObs.addAll(newCat.getAllSubcategories());
            }
        });

        subcategoryBox.valueProperty().addListener((obs, oldV, newSub) -> {
            itemsObs.clear();
            if (newSub != null) {
                itemsObs.addAll(newSub.getAllItems());
            }
        });

        HBox selectors = new HBox(10,
                new Label("Store:"), storeBox,
                new Label("Category:"), categoryBox,
                new Label("Subcategory:"), subcategoryBox);
        selectors.setPadding(new Insets(10));
        selectors.setAlignment(Pos.CENTER_LEFT);

        // Items table (read-only)
        TableView<Item> itemTable = new TableView<>(itemsObs);
        itemTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Item, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getItemCode()));

        TableColumn<Item, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getItemName()));

        TableColumn<Item, Number> invCol = new TableColumn<>("Available Qty");
        invCol.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getCurrentInventoryLevel()));

        itemTable.getColumns().addAll(codeCol, nameCol, invCol);

        // Search + sort controls
        TextField searchField = new TextField();
        searchField.setPromptText("Search item by name (partial match)");
        Button searchBtn = new Button("Search");
        Button resetBtn = new Button("Reset");
        Button sortBtn = new Button("Sort A→Z");

        searchBtn.setOnAction(e -> {
            Store selectedStore = storeBox.getValue();
            if (selectedStore == null) return;
            String term = searchField.getText();
            if (term == null || term.isBlank()) return;

            List<Item> matches = new ArrayList<>();
            for (Category c : selectedStore.getAllCategories()) {
                for (Subcategory s : c.getAllSubcategories()) {
                    matches.addAll(s.searchItems(term));
                }
            }
            itemsObs.setAll(matches);
        });

        resetBtn.setOnAction(e -> {
            searchField.clear();
            Store st = storeBox.getValue();
            if (st == null) {
                itemsObs.clear();
                return;
            }
            itemsObs.clear();
            for (Category c : st.getAllCategories()) {
                for (Subcategory s : c.getAllSubcategories()) {
                    itemsObs.addAll(s.getAllItems());
                }
            }
        });

        sortBtn.setOnAction(e -> {
            FXCollections.sort(itemsObs, Comparator.comparing(Item::getItemName));
        });

        HBox searchBox = new HBox(5, searchField, searchBtn, resetBtn, sortBtn);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(0, 10, 10, 10));

        VBox center = new VBox(5, selectors, searchBox, itemTable);
        center.setPadding(new Insets(5));

        root.setCenter(center);
    }

    private void refreshStores() {
        storesObs.setAll(system.getAllStores());
    }
}

