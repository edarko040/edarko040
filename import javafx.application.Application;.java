import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.plaf.ComboBoxUI;

public class Main extends Application {
    private static final String URL = "jdbc:mysql://localhost:3306/inventory_db";
    private static final String USERNAME = "Darko Emmanuel";
    private static final String PASSWORD = "Password";

    private Inventory inventory = new Inventory();

    public static void main(String[] args) {
        launch(args);
    }

    private static void launch(String[] args) {
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Provision Store Inventory Management System");

        GridPane gridPane = createGridPane();
        addLabelsAndFields(gridPane);

        Button addButton = new Button("Add Goods");
        addButton.setOnAction(event -> addGoods(gridPane));
        gridPane.add(addButton, 0, 5);

        Scene scene = new Scene(gridPane, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private GridPane createGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        return gridPane;
    }

    private void addLabelsAndFields(GridPane gridPane) {
        gridPane.add(new Label("Name:"), 0, 0);
        TextField nameField = new TextField();
        gridPane.add(nameField, 1, 0);

        gridPane.add(new Label("Price:"), 0, 1);
        TextField priceField = new TextField();
        gridPane.add(priceField, 1, 1);

        gridPane.add(new Label("Quantity:"), 0, 2);
        TextField quantityField = new TextField();
        gridPane.add(quantityField, 1, 2);

        gridPane.add(new Label("Category:"), 0, 3);
        ComboBoxUI<String> categoryComboBox = new ComboBoxUI<>(FXCollections.observableArrayList(
                "Beverages", "Bread/Bakery", "Canned/Jarred Goods", "Dairy Products",
                "Dry/Baking Goods", "Frozen Products", "Meat", "Farm Produce",
                "Home Cleaners", "Paper Goods", "Home Care")
        );
        categoryComboBox.getSelectionModel().selectFirst();
        gridPane.add(categoryComboBox, 1, 3);

        gridPane.add(new Label("Date (yyyy-MM-dd):"), 0, 4);
        TextField dateField = new TextField();
        gridPane.add(dateField, 1, 4);
    }

    private void addGoods(GridPane gridPane) {
        String name = ((TextField) gridPane.getChildren().get(1)).getText();
        double price = Double.parseDouble(((TextField) gridPane.getChildren().get(3)).getText());
        int quantity = Integer.parseInt(((TextField) gridPane.getChildren().get(5)).getText());
        String category = ((ComboBox<?>) gridPane.getChildren().get(7)).getValue().toString();
        String dateStr = ((TextField) gridPane.getChildren().get(9)).getText();

        Date date = parseDate(dateStr);

        Good good = new Good(name, price, quantity, category, date);
        inventory.addGood(good);
        DatabaseManager.addGood(good); // Add the good to the database
        System.out.println("Good added successfully!");

        // Show a confirmation dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Good added successfully!");
        alert.showAndWait();
    }

    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            System.out.println("Invalid date format. Defaulting to current date.");
            return new Date();
        }
    }

    private void viewVendors() {
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            String query = "SELECT * FROM vendors";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                int vendorId = resultSet.getInt("vendor_id");
                String name = resultSet.getString("name");
                String address = resultSet.getString("address");
                String phone = resultSet.getString("phone");

                System.out.println("Vendor ID: " + vendorId);
                System.out.println("Name: " + name);
                System.out.println("Address: " + address);
                System.out.println("Phone: " + phone);
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void viewGoods() {
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            String query = "SELECT * FROM goods";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                int goodId = resultSet.getInt("good_id");
                String name = resultSet.getString("name");
                double price = resultSet.getDouble("price");
                int quantity = resultSet.getInt("quantity");
                String category = resultSet.getString("category");
                Date date = resultSet.getDate("date");

                Good good = new Good(name, price, quantity, category, date);
                inventory.addGood(good);

                System.out.println("Good ID: " + goodId);
                System.out.println("Name: " + name);
                System.out.println("Price: " + price);
                System.out.println("Quantity: " + quantity);
                System.out.println("Category: " + category);
                System.out.println("Date: " + date);
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void viewBills() {
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            String query = "SELECT * FROM bills";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                int billId = resultSet.getInt("bill_id");
                int vendorId = resultSet.getInt("vendor_id");
                double amount = resultSet.getDouble("amount");
                Date date = resultSet.getDate("date");

                System.out.println("Bill ID: " + billId);
                System.out.println("Vendor ID: " + vendorId);
                System.out.println("Amount: " + amount);
                System.out.println("Date: " + date);
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void issueGoods() {
        System.out.println("Enter the name of the good to issue: ");
        String name = scanner.nextLine();

        Good good = inventory.searchGood(name);
        if (good == null) {
            System.out.println("Good not found in the inventory.");
            return;
        }

        System.out.println("Enter the quantity to issue: ");
        int quantity = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        if (good.getQuantity() < quantity) {
            System.out.println("Insufficient stock. Only " + good.getQuantity() + " available.");
            return;
        }

        good.decreaseQuantity(quantity);
        inventory.updateGood(good);

        System.out.println(quantity + " " + good.getName() + " issued successfully!");
        SalesManager.addSale(good.getName()); // Add the sale to sales data
    }

    private void viewIssuedGoods() {
        List<String> issuedGoods = SalesManager.getSalesData();
        if (issuedGoods.isEmpty()) {
            System.out.println("No goods have been issued.");
        } else {
            System.out.println("Issued Goods:");
            for (String item : issuedGoods) {
                System.out.println("Item: " + item);
            }
        }
    }


    private void generateSalesReport() {
        System.out.println("Sales Report:");
        SalesManager.generateSalesReport();
    }

    private void generateStockReport() {
        System.out.println("Stock Report:");
        inventory.generateStockReport();
    }
}

class Inventory {
    private List<Good> goods;

    public Inventory() {
        goods = new ArrayList<>();
    }

    public void addGood(Good good) {
        goods.add(good);
    }

    public void updateGood(Good good) {
        for (int i = 0; i < goods.size(); i++) {
            if (goods.get(i).getName().equalsIgnoreCase(good.getName())) {
                goods.set(i, good);
                break;
            }
        }
    }

    public List<Good> getGoods() {
        return goods;
    }

    public Good searchGood(String name) {
        for (Good good : goods) {
            if (good.getName().equalsIgnoreCase(name)) {
                return good;
            }
        }
        return null;
    }

    public void sortGoodsAlphabetically() {
        Collections.sort(goods, Comparator.comparing(Good::getName));
    }

    public void generateStockReport() {
        for (Good good : goods) {
            System.out.println("Name: " + good.getName() + ", Price: " + good.getPrice() +
                    ", Quantity: " + good.getQuantity() + ", Category: " + good.getCategory());
        }
    }
}

class Good {
    private String name;
    private double price;
    private int quantity;
    private String category;
    private Date date;

    public Good(String name, double price, int quantity, String category, Date date) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.date = date;
    }

    // Getters and setters for the properties

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getCategory() {
        return category;
    }

    public void decreaseQuantity(int quantityToDecrease) {
        quantity -= quantityToDecrease;
    }
}