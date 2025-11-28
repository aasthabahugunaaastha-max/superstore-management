package com.superstore.ui;

import com.superstore.core.SuperstoreSystem;
import com.superstore.domain.Warehouse;
import com.superstore.model.Category;
import com.superstore.model.Item;
import com.superstore.model.Subcategory;
import com.superstore.user.WarehouseAdministrator;
import com.superstore.user.WarehouseKeeper;
import com.superstore.user.User;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WarehouseAdminDashboard {

    private final BorderPane root;
    private final SuperstoreSystem system;
    private final WarehouseAdministrator admin;
    private final Warehouse warehouse;
    private final MainApp app;

    // CLASS FIELDS
    private ComboBox<Category> categoryBox;
    private ComboBox<Subcategory> subcategoryBox;
    private TableView<Item> itemsTable;
    
    // ✅ KEEPER FIELDS (SEPARATE)
    private ComboBox<Category> keeperCategoryBox;
    private ComboBox<Subcategory> keeperSubcategoryBox;
    private ListView<String> keepersListView;

    // MAIN OBSERVABLES
    private final ObservableList<Category> categoriesObs = FXCollections.observableArrayList();
    private final ObservableList<Subcategory> subcategoriesObs = FXCollections.observableArrayList();
    private final ObservableList<Item> itemsObs = FXCollections.observableArrayList();
    private final ObservableList<String> keepersObs = FXCollections.observableArrayList();
    
    // ✅ KEEPER-SPECIFIC OBSERVABLES (separate to avoid cross-contamination)
    private final ObservableList<Category> keeperCategoriesObs = FXCollections.observableArrayList();
    private final ObservableList<Subcategory> keeperSubcategoriesObs = FXCollections.observableArrayList();

    // ✅ FIXED: Store BOTH cost AND description independently
    private final Map<Item, Double> itemCosts = new HashMap<>();
    private final Map<Item, String> itemDescriptions = new HashMap<>();

    public WarehouseAdminDashboard(MainApp app, SuperstoreSystem system, WarehouseAdministrator admin) {
        this.app = app;
        this.system = system;
        this.admin = admin;
        this.warehouse = system.getWarehouse(admin.getFacilityId());
        this.root = new BorderPane();
        build();
        refreshAllData();
    }

    public BorderPane getRoot() {
        return root;
    }

    private void build() {
        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> app.showLoginView());
        
        HBox topBar = new HBox(10, 
            new Label("Warehouse Admin: " + admin.getUsername() + " | " + warehouse.getWarehouseName()), 
            logoutBtn);
        topBar.setPadding(new Insets(5));
        root.setTop(topBar);

        VBox left = createCategoryManagementPanel();
        VBox center = createItemManagementPanel();
        VBox right = createKeeperAndOrdersPanel();

        SplitPane split = new SplitPane(left, center, right);
        split.setDividerPositions(0.22, 0.62);
        root.setCenter(split);
    }

    private VBox createCategoryManagementPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(10));
        panel.setSpacing(10);

        this.categoryBox = new ComboBox<>(categoriesObs);
        this.categoryBox.setPromptText("Select Category");
        this.categoryBox.setMinWidth(160);

        this.subcategoryBox = new ComboBox<>(subcategoriesObs);
        this.subcategoryBox.setPromptText("Select Subcategory");
        this.subcategoryBox.setMinWidth(160);

        setupComboBoxDisplay(this.categoryBox, Category::getCategoryName);
        setupComboBoxDisplay(this.subcategoryBox, Subcategory::getSubcategoryName);

        this.categoryBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            subcategoriesObs.clear();
            itemsObs.clear();
            if (newVal != null) subcategoriesObs.setAll(newVal.getAllSubcategories());
            refreshInventoryData();
        });

        this.subcategoryBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            itemsObs.clear();
            if (newVal != null) itemsObs.setAll(newVal.getAllItems());
        });

        HBox catButtons = new HBox(5,
            createSmallButton("Add", this::addCategory),
            createSmallButton("Edit", this::editCategory),
            createSmallButton("Delete", this::deleteCategory)
        );

        HBox subButtons = new HBox(5,
            createSmallButton("Add", this::addSubcategory),
            createSmallButton("Edit", this::editSubcategory),
            createSmallButton("Delete", this::deleteSubcategory)
        );

        panel.getChildren().addAll(
            new Label("📁 Category Mgmt"),
            this.categoryBox, catButtons,
            this.subcategoryBox, subButtons
        );
        return panel;
    }

    private VBox createItemManagementPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(10));

        this.itemsTable = new TableView<>(itemsObs);
        this.itemsTable.setEditable(true);

        TableColumn<Item, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getItemCode()));

        TableColumn<Item, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getItemName()));

        TableColumn<Item, String> descCol = new TableColumn<>("Desc");
        descCol.setCellValueFactory(c -> new SimpleStringProperty(getDescription(c.getValue())));
        descCol.setCellFactory(TextFieldTableCell.forTableColumn());
        descCol.setOnEditCommit(e -> {
            setDescription(e.getRowValue(), e.getNewValue());
            refreshInventoryData();
        });

        TableColumn<Item, Double> costCol = new TableColumn<>("Cost/Unit");
        costCol.setCellValueFactory(c -> new SimpleDoubleProperty(getCostPerUnit(c.getValue())).asObject());
        costCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        costCol.setOnEditCommit(e -> {
            Double newValue = e.getNewValue();
            if (newValue != null) {
                setCostPerUnit(e.getRowValue(), newValue);
                refreshInventoryData();
            }
        });

        TableColumn<Item, Integer> invCol = new TableColumn<>("Inv");
        invCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCurrentInventoryLevel()).asObject());
        invCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        invCol.setOnEditCommit(e -> {
            Integer newValue = e.getNewValue();
            if (newValue != null) {
                e.getRowValue().setCurrentInventoryLevel(newValue);
                refreshInventoryData();
            }
        });

        TableColumn<Item, Double> eoqCol = new TableColumn<>("EOQ");
        eoqCol.setCellValueFactory(c -> new SimpleDoubleProperty(
            Math.sqrt(2 * getCostPerUnit(c.getValue()) * 100.0 * c.getValue().getCurrentInventoryLevel() / 2.0)).asObject());

        this.itemsTable.getColumns().addAll(codeCol, nameCol, descCol, costCol, invCol, eoqCol);

        HBox itemButtons = new HBox(5,
            createSmallButton("➕Item", this::addItem),
            createSmallButton("🗑️Item", this::deleteItem),
            createSmallButton("+10", () -> adjustInventory(10)),
            createSmallButton("-10", () -> adjustInventory(-10))
        );

        panel.getChildren().addAll(new Label("📦 Item Mgmt (Edit All)"), this.itemsTable, itemButtons);
        return panel;
    }

    private VBox createKeeperAndOrdersPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(10));

        // ✅ SEPARATE keeper combos
        this.keeperCategoryBox = new ComboBox<>(keeperCategoriesObs);
        this.keeperCategoryBox.setPromptText("Keeper Category");
        this.keeperCategoryBox.setMinWidth(140);
        setupComboBoxDisplay(this.keeperCategoryBox, Category::getCategoryName);

        this.keeperSubcategoryBox = new ComboBox<>(keeperSubcategoriesObs);
        this.keeperSubcategoryBox.setPromptText("Keeper Subcategory");
        this.keeperSubcategoryBox.setMinWidth(140);
        setupComboBoxDisplay(this.keeperSubcategoryBox, Subcategory::getSubcategoryName);

        this.keepersListView = new ListView<>(keepersObs);
        this.keepersListView.setPrefHeight(120);

        // ✅ FIXED: Use SEPARATE keeperSubcategoriesObs
        this.keeperCategoryBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            keeperSubcategoriesObs.clear();
            if (newVal != null) keeperSubcategoriesObs.setAll(newVal.getAllSubcategories());
        });

        HBox keeperButtons = new HBox(5,
            createSmallButton("➕Add", this::addKeeper),
            createSmallButton("✏️Edit", this::editKeeper),
            createSmallButton("🗑️Delete", this::deleteKeeper)
        );

        VBox keeperSection = new VBox(5,
            new Label("👷 Warehouse Keepers"),
            this.keeperCategoryBox, this.keeperSubcategoryBox,
            keepersListView, keeperButtons
        );

        Label reorderLabel = new Label("Reorder: 0");
        VBox reorderSection = new VBox(5, new Label("🔄 Reorder Alerts"), reorderLabel);

        panel.getChildren().addAll(keeperSection, new Separator(), reorderSection);
        return panel;
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

    // ========== KEEPER OPERATIONS (EXACTLY LIKE StoreAdminDashboard) ==========
    private void addKeeper() {
        Category cat = keeperCategoryBox.getValue();
        Subcategory sub = keeperSubcategoryBox.getValue();
        if (cat == null || sub == null) {
            showError("Select category AND subcategory first");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Keeper Name");
        dialog.setContentText("Base username (will be: wk_username):");
        dialog.showAndWait().ifPresent(baseUsername -> {
            if (!baseUsername.trim().isEmpty()) {
                String uniqueUsername = "wk_" + baseUsername.trim();
                
                // ✅ CREATE REAL WarehouseKeeper in system (PERSISTENT)
                WarehouseKeeper keeper = new WarehouseKeeper(
                    "WK" + System.currentTimeMillis(), 
                    uniqueUsername, 
                    "password", 
                    warehouse.getWarehouseId()
                );
                system.createUser(keeper);
                
                // ✅ Add to UI list with category assignment
                String keeperEntry = uniqueUsername + " (" + 
                                   cat.getCategoryName() + " > " + sub.getSubcategoryName() + ")";
                keepersObs.add(keeperEntry);
                
                //showInfo("Keeper " + uniqueUsername + " created and assigned!");
            }
        });
    }

    private void editKeeper() {
        // Optional - same as before
    }

    private void deleteKeeper() {
        String selected = keepersListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a keeper first");
            return;
        }
        
        String username = selected.split(" \\(")[0].trim();
        
        List<User> keepers = system.getAllUsers().stream()
            .filter(u -> u instanceof WarehouseKeeper)
            .collect(Collectors.toList());

        for (User u : keepers) {
            WarehouseKeeper wk = (WarehouseKeeper) u;
            if (wk.getUsername().equals(username) && wk.getFacilityId().equals(warehouse.getWarehouseId())) {
                system.deleteUser(u);  // ✅ DELETE FROM SYSTEM
                refreshKeeperData();   // ✅ REFRESH FROM SYSTEM
                return;
            }
        }
    }

    private void refreshKeeperData() {
        keepersObs.clear();
        
        // ✅ LOAD REAL keepers from system
        List<User> allUsers = system.getAllUsers();
        for (User user : allUsers) {
            if (user instanceof WarehouseKeeper) {
                WarehouseKeeper wk = (WarehouseKeeper) user;
                if (wk.getFacilityId().equals(warehouse.getWarehouseId())) {
                    // ✅ Show with category assignment (demo mapping)
                    String categoryDisplay = getKeeperCategoryDisplay(wk); 
                    String display = wk.getUsername() + " (" + categoryDisplay + ")";
                    keepersObs.add(display);
                }
            }
        }
    }

    private String getKeeperCategoryDisplay(WarehouseKeeper wk) {
        // Demo: Map username to category (replace with real mapping later)
        String username = wk.getUsername();
        if (username.contains("dairy") || username.contains("milk")) return "Grocery > Dairy";
        if (username.contains("phone")) return "Electronics > Phones";
        return "Warehouse-Wide"; // Default
    }

    // ========== ITEM OPERATIONS (unchanged) ==========
    private void addItem() {
        Subcategory sub = subcategoryBox.getValue();
        if (sub == null) {
            showError("Select subcategory first");
            return;
        }

        Dialog<ItemInput> dialog = new Dialog<>();
        dialog.setTitle("Add New Item");
        dialog.setHeaderText("Enter item details:");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField nameField = new TextField();
        TextField costField = new TextField("10.0");
        TextField descField = new TextField();

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Cost/Unit:"), 0, 1);
        grid.add(costField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    String name = nameField.getText().trim();
                    double cost = Double.parseDouble(costField.getText());
                    String desc = descField.getText().trim();
                    
                    if (name.isEmpty()) {
                        showError("Item name is required");
                        return null;
                    }
                    return new ItemInput(name, cost, desc);
                } catch (NumberFormatException e) {
                    showError("Invalid cost format. Use numbers only.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(input -> {
            String code = "ITEM" + System.currentTimeMillis();
            Item item = new Item(code, input.name, input.description);
            
            itemCosts.put(item, input.cost);
            itemDescriptions.put(item, input.description);
            
            sub.addItem(item);
            refreshInventoryData();
        });
    }

    private static record ItemInput(String name, double cost, String description) {}

    private String getDescription(Item item) {
        return itemDescriptions.getOrDefault(item, item.getDescription());
    }

    private void setDescription(Item item, String description) {
        itemDescriptions.put(item, description);
    }

    private double getCostPerUnit(Item item) {
        return itemCosts.getOrDefault(item, 10.0);
    }

    private void setCostPerUnit(Item item, double cost) {
        itemCosts.put(item, cost);
    }

    // ========== OTHER CRUD (unchanged) ==========
    private void deleteItem() {
        Item selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { 
            showError("Select item first"); 
            return; 
        }
        itemCosts.remove(selected);
        itemDescriptions.remove(selected);
        refreshInventoryData();
    }

    private void adjustInventory(int delta) {
        Item selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.addInventory(delta);
            refreshInventoryData();
        } else {
            showError("Select item first");
        }
    }

    private void addCategory() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("New Category Name");
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                String id = "CAT" + System.currentTimeMillis();
                warehouse.addCategory(new Category(id, name));
                refreshStructureData();
                refreshKeeperStructureData();
            }
        });
    }

    private void editCategory() {
        Category selected = categoryBox.getValue();
        if (selected == null) { showError("Select category first"); return; }
        TextInputDialog dialog = new TextInputDialog(selected.getCategoryName());
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                selected.setCategoryName(name);
                refreshStructureData();
            }
        });
    }

    private void deleteCategory() {
        Category selected = categoryBox.getValue();
        if (selected == null) { showError("Select category first"); return; }
        refreshStructureData();
    }

    private void addSubcategory() {
        Category cat = categoryBox.getValue();
        if (cat == null) { showError("Select category first"); return; }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("New Subcategory");
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                String id = "SUB" + System.currentTimeMillis();
                Subcategory sub = new Subcategory(id, name, cat.getCategoryId());
                cat.addSubcategory(sub);
                refreshStructureData();
            }
        });
    }

    private void editSubcategory() {
        Subcategory selected = subcategoryBox.getValue();
        if (selected == null) { showError("Select subcategory first"); return; }
        TextInputDialog dialog = new TextInputDialog(selected.getSubcategoryName());
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                selected.setSubcategoryName(name);
                refreshStructureData();
            }
        });
    }

    private void deleteSubcategory() {
        Subcategory selected = subcategoryBox.getValue();
        if (selected == null) { showError("Select subcategory first"); return; }
        refreshStructureData();
    }

    // ========== REFRESH METHODS ==========
    private void refreshAllData() { 
        categoriesObs.setAll(warehouse.getAllCategories());
        keeperCategoriesObs.setAll(warehouse.getAllCategories());  // ✅ Keeper categories
        subcategoriesObs.clear();
        itemsObs.clear();
        refreshKeeperData();  // ✅ Load persistent keepers
    }
    
    private void refreshStructureData() {
        categoriesObs.setAll(warehouse.getAllCategories());
        keeperCategoriesObs.setAll(warehouse.getAllCategories());  // ✅ Keeper categories
    }
    
    private void refreshKeeperStructureData() {
        keeperCategoriesObs.setAll(warehouse.getAllCategories());
        keeperSubcategoriesObs.clear();
    }
    
    private void refreshInventoryData() {
        if (subcategoryBox.getValue() != null) {
            itemsObs.setAll(subcategoryBox.getValue().getAllItems());
        } else if (categoryBox.getValue() != null) {
            Stream<Item> allItems = categoryBox.getValue().getAllSubcategories().stream()
                .flatMap(s -> s.getAllItems().stream());
            itemsObs.setAll(allItems.toList());
        }
    }

    private Button createSmallButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setOnAction(e -> action.run());
        btn.setMinWidth(65);
        btn.setStyle("-fx-font-size: 9px;");
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
}

