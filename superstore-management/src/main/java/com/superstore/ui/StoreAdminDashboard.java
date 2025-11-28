package com.superstore.ui;
import com.superstore.domain.Warehouse;
import com.superstore.core.SuperstoreSystem;
import com.superstore.domain.Store;
import com.superstore.model.Category;
import com.superstore.model.Item;
import com.superstore.model.Subcategory;
import com.superstore.user.StoreAdministrator;
import com.superstore.user.Storekeeper;
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
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StoreAdminDashboard {

    private final BorderPane root;
    private final SuperstoreSystem system;
    private final StoreAdministrator admin;
    private final Store store;
    private final MainApp app;
    

    // CLASS FIELDS
    private ComboBox<Category> categoryBox;
    private ComboBox<Subcategory> subcategoryBox;
    private TableView<Item> itemsTable;
    private ListView<String> keepersListView;
    
    // ORDER MANAGEMENT FIELDS (SIMPLIFIED)
    private ComboBox<String> warehouseSelectBox;
    private ComboBox<Category> warehouseCategoryBox;
    private ComboBox<Subcategory> warehouseSubcategoryBox;
    private TableView<OrderRow> ordersTable;

    private final ObservableList<Category> categoriesObs = FXCollections.observableArrayList();
    private final ObservableList<Subcategory> subcategoriesObs = FXCollections.observableArrayList();
    private final ObservableList<Item> itemsObs = FXCollections.observableArrayList();
    private final ObservableList<String> keepersObs = FXCollections.observableArrayList();
    
    // ORDER MANAGEMENT OBSERVABLES (SIMPLIFIED)
    private final ObservableList<String> warehousesObs = FXCollections.observableArrayList();
    private final ObservableList<Category> warehouseCategoriesObs = FXCollections.observableArrayList();
    private final ObservableList<Subcategory> warehouseSubcategoriesObs = FXCollections.observableArrayList();
    private final ObservableList<OrderRow> ordersObs = FXCollections.observableArrayList();

    // ✅ FIXED: Store BOTH cost AND description independently
    private final Map<Item, Double> itemCosts = new HashMap<>();
    private final Map<Item, String> itemDescriptions = new HashMap<>();
    
    private ComboBox<Category> keeperCategoryBox;
    private ComboBox<Subcategory> keeperSubcategoryBox;
    private final ObservableList<Category> keeperCategoriesObs = FXCollections.observableArrayList();
    private final ObservableList<Subcategory> keeperSubcategoriesObs = FXCollections.observableArrayList();

    // ✅ SIMPLIFIED OrderRow class (NO external Order dependency)
    public static class OrderRow {
        private final String orderId;
        private final String itemCode;
        private final String itemName;
        private int quantity;
        private String status;

        public OrderRow(String orderId, String itemCode, String itemName, int quantity, String status) {
            this.orderId = orderId;
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.quantity = quantity;
            this.status = status;
        }

        // Getters
        public String getOrderId() { return orderId; }
        public String getItemCode() { return itemCode; }
        public String getItemName() { return itemName; }
        public int getQuantity() { return quantity; }
        public String getStatus() { return status; }
        
        // Setters
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public void setStatus(String status) { this.status = status; }
    }

    public StoreAdminDashboard(MainApp app, SuperstoreSystem system, StoreAdministrator admin) {
        this.app = app;
        this.system = system;
        this.admin = admin;
        this.store = system.getStore(admin.getFacilityId());
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
            new Label("Store Admin: " + admin.getUsername() + " | " + store.getStoreName()), 
            logoutBtn);
        topBar.setPadding(new Insets(5));
        root.setTop(topBar);

        VBox left = createCategoryManagementPanel();
        VBox center = createItemManagementPanel();
        VBox right = createKeeperAndOrdersPanel();
        VBox orderPane = createOrderManagementPanel();

        SplitPane split = new SplitPane(left, center, right, orderPane);
        split.setDividerPositions(0.14, 0.46, 0.72, 1.0);
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

	    // ✅ SEPARATE keeper combos - use dedicated ObservableLists
	    keeperCategoryBox = new ComboBox<>(keeperCategoriesObs);
	    keeperCategoryBox.setPromptText("Keeper Category");
	    keeperCategoryBox.setMinWidth(140);
	    setupComboBoxDisplay(keeperCategoryBox, Category::getCategoryName);

	    keeperSubcategoryBox = new ComboBox<>(keeperSubcategoriesObs);
	    keeperSubcategoryBox.setPromptText("Keeper Subcategory");
	    keeperSubcategoryBox.setMinWidth(140);
	    setupComboBoxDisplay(keeperSubcategoryBox, Subcategory::getSubcategoryName);

	    keepersListView = new ListView<>(keepersObs);
	    keepersListView.setPrefHeight(120);

	    // ✅ FIXED: Use keeperSubcategoriesObs (separate from main subcategoriesObs)
	    keeperCategoryBox.valueProperty().addListener((obs, oldVal, newVal) -> {
		keeperSubcategoriesObs.clear();  // ✅ Only affects keeper combos
		if (newVal != null) {
		    keeperSubcategoriesObs.setAll(newVal.getAllSubcategories());
		}
	    });

	    HBox keeperButtons = new HBox(5,
		createSmallButton("➕Add", this::addStoreKeeper),
		createSmallButton("✏️Edit", this::editStoreKeeper),
		createSmallButton("🗑️Delete", this::deleteStoreKeeper)
	    );

	    VBox keeperSection = new VBox(5,
		new Label("👷 Store Keepers"),
		keeperCategoryBox, keeperSubcategoryBox,
		keepersListView, keeperButtons
	    );

	    Label reorderLabel = new Label("Reorder: 0");
	    VBox reorderSection = new VBox(5, new Label("🔄 Reorder Alerts"), reorderLabel);

	    panel.getChildren().addAll(keeperSection, new Separator(), reorderSection);
	    return panel;
	}



    private VBox createOrderManagementPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(10));

        this.warehouseSelectBox = new ComboBox<>(warehousesObs);
        this.warehouseSelectBox.setPromptText("Select Warehouse");
        setupComboBoxDisplayString(this.warehouseSelectBox);

        this.warehouseCategoryBox = new ComboBox<>(warehouseCategoriesObs);
        this.warehouseCategoryBox.setPromptText("Select Category");
        setupComboBoxDisplay(this.warehouseCategoryBox, Category::getCategoryName);

        this.warehouseSubcategoryBox = new ComboBox<>(warehouseSubcategoriesObs);
        this.warehouseSubcategoryBox.setPromptText("Select Subcategory");
        setupComboBoxDisplay(this.warehouseSubcategoryBox, Subcategory::getSubcategoryName);

        this.ordersTable = new TableView<>(ordersObs);
        this.ordersTable.setEditable(true);

        TableColumn<OrderRow, String> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOrderId()));

        TableColumn<OrderRow, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getItemCode() + " - " + c.getValue().getItemName()));

        TableColumn<OrderRow, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantity()).asObject());
        qtyCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        qtyCol.setOnEditCommit(e -> {
            int newQty = e.getNewValue();
            if (newQty > 0) {
                e.getRowValue().setQuantity(newQty);
                refreshOrders();
            } else {
                showError("Quantity must be positive");
                refreshOrders();
            }
        });

        TableColumn<OrderRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        this.ordersTable.getColumns().addAll(orderIdCol, itemCol, qtyCol, statusCol);

        HBox orderButtons = new HBox(5,
            createSmallButton("➕Order", this::addOrder),
            createSmallButton("🗑️Order", this::deleteOrder)
        );

        // EVENT LISTENERS
       this.warehouseSelectBox.valueProperty().addListener((obs, old, newVal) -> {
	    warehouseCategoriesObs.clear();
	    warehouseSubcategoriesObs.clear();
	    ordersObs.clear();
	    if (newVal != null && !newVal.startsWith("No")) {
		String warehouseId = newVal.split(" - ")[0];
		try {
		    Warehouse wh = system.getWarehouse(warehouseId);
		    if (wh != null) {
		        // ✅ Load REAL warehouse categories
		        warehouseCategoriesObs.setAll(wh.getAllCategories());
		    } else {
		        // Fallback to store categories
		        warehouseCategoriesObs.setAll(store.getAllCategories());
		    }
		} catch (Exception e) {
		    warehouseCategoriesObs.setAll(store.getAllCategories());
		}
	    }
	});

        this.warehouseCategoryBox.valueProperty().addListener((obs, old, newVal) -> {
            warehouseSubcategoriesObs.clear();
            ordersObs.clear();
            if (newVal != null) warehouseSubcategoriesObs.setAll(newVal.getAllSubcategories());
        });

        this.warehouseSubcategoryBox.valueProperty().addListener((obs, old, newVal) -> {
            refreshOrders();
        });

        panel.getChildren().addAll(
            new Label("📋 Orders to Warehouse"),
            warehouseSelectBox, warehouseCategoryBox, warehouseSubcategoryBox,
            ordersTable, orderButtons
        );
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

    private void setupComboBoxDisplayString(ComboBox<String> comboBox) {
        comboBox.setCellFactory(lv -> new ListCell<String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
            }
        });
        comboBox.setButtonCell(new ListCell<String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
            }
        });
    }

    // ========== ITEM OPERATIONS ==========
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

    // ========== STORE KEEPER OPERATIONS ==========
    private void addStoreKeeper() {
	    Category cat = keeperCategoryBox.getValue();
	    Subcategory sub = keeperSubcategoryBox.getValue();
	    if (cat == null || sub == null) {
		showError("Select category AND subcategory first");
		return;
	    }

	    TextInputDialog dialog = new TextInputDialog();
	    dialog.setHeaderText("Keeper Name");
	    dialog.setContentText("Base username (will be: sk_username):");
	    dialog.showAndWait().ifPresent(baseUsername -> {
		if (!baseUsername.trim().isEmpty()) {
		    String uniqueUsername = "sk_" + baseUsername.trim();
		    
		    // ✅ CREATE REAL Storekeeper in system (PERSISTENT)
		    Storekeeper keeper = new Storekeeper(
		        "SK" + System.currentTimeMillis(), 
		        uniqueUsername, 
		        "password", 
		        store.getStoreId()
		    );
		    system.createUser(keeper);
		    
		    // ✅ Add to UI list with category assignment
		    String keeperEntry = uniqueUsername + " (" + 
		                       cat.getCategoryName() + " > " + sub.getSubcategoryName() + ")";
		    keepersObs.add(keeperEntry);
		    
		   // showInfo("Keeper " + uniqueUsername + " created and assigned!");
		}
	    });
	}




    private void editStoreKeeper() {
        // Optional edit implementation
    }

	private void deleteStoreKeeper() {
	    String selected = keepersListView.getSelectionModel().getSelectedItem();
	    if (selected == null) {
		showError("Select a keeper first");
		return;
	    }
	    
	    String username = selected.split(" \\(")[0].trim();
	    
	    List<User> keepers = system.getAllUsers().stream()
		.filter(u -> u instanceof Storekeeper)
		.collect(Collectors.toList());

	    for (User u : keepers) {
		Storekeeper sk = (Storekeeper) u;
		if (sk.getUsername().equals(username) && sk.getFacilityId().equals(store.getStoreId())) {
		    system.deleteUser(u);  
		    refreshKeeperData();  
		    return;
		}
	    }
	}


    // ========== ORDER OPERATIONS ==========
    private void addOrder() {
        Subcategory sub = warehouseSubcategoryBox.getValue();
        String warehouseId = warehouseSelectBox.getValue();
        if (sub == null || warehouseId == null) {
            showError("Select warehouse and subcategory first");
            return;
        }

        List<Item> items = sub.getAllItems();
        if (items.isEmpty()) {
            showError("No items in this subcategory");
            return;
        }

        // Simple item selection - use first item for demo
        Item item = items.get(0);
        TextInputDialog qtyDialog = new TextInputDialog("5");
        qtyDialog.setHeaderText("Order quantity for: " + item.getItemName());
        qtyDialog.setContentText("Quantity:");
        qtyDialog.showAndWait().ifPresent(qtyStr -> {
            try {
                int qty = Integer.parseInt(qtyStr);
                if (qty > 0) {
                    String orderId = "ORD" + System.currentTimeMillis();
                    OrderRow newOrder = new OrderRow(
                        orderId, item.getItemCode(), item.getItemName(), qty, "Pending");
                    ordersObs.add(newOrder);
                    showInfo("Order " + orderId + " created!");
                } else {
                    showError("Quantity must be positive");
                }
            } catch (NumberFormatException e) {
                showError("Invalid quantity");
            }
        });
    }

    private void deleteOrder() {
        OrderRow selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select an order first");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Delete order: " + selected.getOrderId() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ordersObs.remove(selected);
                showInfo("Order deleted!");
            }
        });
    }

    private void refreshOrders() {
        // Demo data - replace with real system.getOrders() when backend ready
        ordersObs.clear();
        if (warehouseSubcategoryBox.getValue() != null) {
            ordersObs.addAll(
                new OrderRow("ORD001", "ITEM001", "Milk", 10, "Pending"),
                new OrderRow("ORD002", "ITEM002", "Bread", 20, "Shipped")
            );
        }
    }

    // ========== OTHER CRUD ==========
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
                store.addCategory(new Category(id, name));
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
	    categoriesObs.setAll(store.getAllCategories());
	    subcategoriesObs.clear();
	    itemsObs.clear();
	    refreshKeeperStructureData(); 
	    refreshKeeperData();
	    refreshWarehouseData();
	}

     private void refreshKeeperStructureData() {
	    keeperCategoriesObs.setAll(store.getAllCategories());
	    keeperSubcategoriesObs.clear();
	}

    private void refreshStructureData() {
        categoriesObs.setAll(store.getAllCategories());
        keeperCategoriesObs.setAll(store.getAllCategories());  

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

   private void refreshKeeperData() {
	    keepersObs.clear();
	    
	    List<User> allUsers = system.getAllUsers();
	    for (User user : allUsers) {
		if (user instanceof Storekeeper) {
		    Storekeeper sk = (Storekeeper) user;
		    if (sk.getFacilityId().equals(store.getStoreId())) {
		        String categoryDisplay = getKeeperCategoryDisplay(sk); 
		        String display = sk.getUsername() + " (" + categoryDisplay + ")";
		        keepersObs.add(display);
		    }
		}
	    }
	}

	// Helper method to show category assignment (demo)
	private String getKeeperCategoryDisplay(Storekeeper sk) {
	    // Demo: Map username to category (replace with real mapping later)
	    String username = sk.getUsername();
	    if (username.contains("dairy") || username.contains("milk")) return "Grocery > Dairy";
	    if (username.contains("phone")) return "Electronics > Phones";
	    return "Store-Wide"; // Default
	}


   private void refreshWarehouseData() {
	    warehousesObs.clear();
	    warehouseCategoriesObs.clear();
	    warehouseSubcategoriesObs.clear();
	    ordersObs.clear();
	    
	    // ✅ PERFECT! Store → ONE Warehouse mapping
	    String warehouseId = store.getLinkedWarehouseId();
	    if (warehouseId != null && !warehouseId.isEmpty()) {
		try {
		    // Get actual warehouse from system
		    Warehouse warehouse = system.getWarehouse(warehouseId);
		    if (warehouse != null) {
		        String display = warehouseId + " - " + warehouse.getWarehouseName();
		        warehousesObs.add(display);
		    } else {
		        // Fallback if warehouse not found
		        warehousesObs.add(warehouseId + " - Linked Warehouse");
		    }
		} catch (Exception e) {
		    // Safe fallback
		    warehousesObs.add(warehouseId + " - Linked Warehouse");
		}
	    } else {
		warehousesObs.add("No warehouse linked");
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



