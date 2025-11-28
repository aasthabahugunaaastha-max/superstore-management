package com.superstore.ui;

import com.superstore.core.SuperstoreSystem;
import com.superstore.user.*;
import com.superstore.user.User.UserType;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage primaryStage;
    private SuperstoreSystem system;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.system = SuperstoreSystem.getInstance();

        system.loadData();

        showLoginView();
    }

    public void showLoginView() {
        LoginView loginView = new LoginView(this, system);
        Scene scene = new Scene(loginView.getRoot(), 400, 250);
        primaryStage.setTitle("Superstore Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void showDashboard(User user) {
        UserType type = user.getUserType();
        switch (type) {
            case SUPER_USER -> showSuperUserDashboard((SuperUser) user);
            case WAREHOUSE_ADMIN -> showWarehouseAdminDashboard((WarehouseAdministrator) user);
            case STORE_ADMIN -> showStoreAdminDashboard((StoreAdministrator) user);
            case WAREHOUSE_KEEPER -> showWarehouseKeeperDashboard((WarehouseKeeper) user);
            case STOREKEEPER -> showStorekeeperDashboard((Storekeeper) user);
            case END_USER -> showEndUserDashboard((EndUser) user);
        }
    }

    private void showSuperUserDashboard(SuperUser superUser) {
        SuperUserDashboard view = new SuperUserDashboard(this, system, superUser);
        Scene scene = new Scene(view.getRoot(), 900, 600);
        primaryStage.setTitle("Super User - Superstore");
        primaryStage.setScene(scene);
    }

    private void showWarehouseAdminDashboard(WarehouseAdministrator admin) {
        WarehouseAdminDashboard view = new WarehouseAdminDashboard(this, system, admin);
        Scene scene = new Scene(view.getRoot(), 900, 600);
        primaryStage.setTitle("Warehouse Admin - Superstore");
        primaryStage.setScene(scene);
    }

    private void showStoreAdminDashboard(StoreAdministrator admin) {
        StoreAdminDashboard view = new StoreAdminDashboard(this, system, admin);
        Scene scene = new Scene(view.getRoot(), 900, 600);
        primaryStage.setTitle("Store Admin - Superstore");
        primaryStage.setScene(scene);
    }

    private void showWarehouseKeeperDashboard(WarehouseKeeper keeper) {
        WarehouseKeeperDashboard view = new WarehouseKeeperDashboard(this, system, keeper);
        Scene scene = new Scene(view.getRoot(), 900, 600);
        primaryStage.setTitle("Warehouse Keeper - Superstore");
        primaryStage.setScene(scene);
    }

    private void showStorekeeperDashboard(Storekeeper keeper) {
        StorekeeperDashboard view = new StorekeeperDashboard(this, system, keeper);
        Scene scene = new Scene(view.getRoot(), 900, 600);
        primaryStage.setTitle("Storekeeper - Superstore");
        primaryStage.setScene(scene);
    }

    private void showEndUserDashboard(EndUser endUser) {
        EndUserDashboard view = new EndUserDashboard(this, system);
        Scene scene = new Scene(view.getRoot(), 900, 600);
        primaryStage.setTitle("End User - Superstore");
        primaryStage.setScene(scene);
    }

    @Override
    public void stop() {
        system.saveData();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

