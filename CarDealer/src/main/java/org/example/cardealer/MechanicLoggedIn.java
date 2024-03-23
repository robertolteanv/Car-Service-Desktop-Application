package org.example.cardealer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

import javafx.scene.image.ImageView;


public class MechanicLoggedIn {
    private SessionManager sessionManager;

    @FXML
    private ImageView profileImageView;

    @FXML
    private Button addCarButton;

    @FXML
    private Button updateStatusButton;

    @FXML
    private Button logOutButton;

    @FXML
    private TableView<Car> carTableView;

    @FXML
    private TableColumn<Car, String> makeTableColumn;

    @FXML
    private TableColumn<Car, String> modelTableColumn;

    @FXML
    private TableColumn<Car, String> vinTableColumn;

    @FXML
    private TableColumn<Car, String> problemTableColumn;

    @FXML
    private TableColumn<Car, String> statusTableColumn;
    @FXML
    private Button seeClientsButton;
    @FXML
    private TableView<Client> clientTableView;

    @FXML
    private TableColumn<Client, Integer> userIdColumn;

    @FXML
    private TableColumn<Client, String> usernameColumn;



    @FXML
    public void initialize() {
        sessionManager = SessionManager.getInstance();
        loadAllCars(); // Call the method to load all cars

        // Add the following line to bind the button
        refreshButton.setOnAction(event -> refreshButtonOnAction());
    }


    private void changeScene(Button button, String fxmlFileName) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
        Parent root = loader.load();

        Stage stage = (Stage) button.getScene().getWindow();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void addCarButtonOnAction() {
        openAddCarScene();
    }

    @FXML
    private Button refreshButton;  // Add this if not already present

    @FXML
    private void refreshButtonOnAction() {
        // Perform the table data refresh
        loadAllCars();
    }

    private void openAddCarScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addCar.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add Car");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteCarButtonOnAction() {
        Car selectedCar = carTableView.getSelectionModel().getSelectedItem();

        if (selectedCar != null) {
            // Confirm the deletion
            boolean confirmed = showConfirmationDialog("Delete Car", "Are you sure you want to delete this car?");

            if (confirmed) {
                // Perform the deletion
                deleteCar(selectedCar);
            }
        } else {
            // No car selected, show a message or alert
            showAlert("No Car Selected", "Please select a car to delete.");
        }
    }

    private boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void deleteCar(Car car) {
        int carId = getCarId(car.getVinNumber());

        if (carId != -1) {
            String deleteCarSQL = "DELETE FROM cars WHERE car_id = ?";

            try (Connection connection = DatabaseConnection.getConnection("postgres", "postgres", "admin");
                 PreparedStatement preparedStatement = connection.prepareStatement(deleteCarSQL)) {

                preparedStatement.setInt(1, carId);

                int affectedRows = preparedStatement.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Car deleted successfully");
                    // Refresh the table after deleting the car
                    loadAllCars();
                } else {
                    System.out.println("Failed to delete car");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void updateStatusButtonOnAction() {
        // Implement logic to update the status of a selected car
        Car selectedCar = carTableView.getSelectionModel().getSelectedItem();
        if (selectedCar != null) {
            // Open a dialog to select the new status
            String newStatus = showStatusInputDialog();

            // Update the status in the database
            if (newStatus != null) {
                updateStatusInDatabase(selectedCar.getVinNumber(), newStatus);
                // Refresh the table after updating the status
                loadAllCars();
            }
        }
    }

    private String showStatusInputDialog() {
        // Open a dialog to select the new status
        String[] statusOptions = {"Scheduled", "In Progress", "Completed"};

        ChoiceDialog<String> dialog = new ChoiceDialog<>(statusOptions[0], Arrays.asList(statusOptions));
        dialog.setTitle("Select Status");
        dialog.setHeaderText("Select the new status:");
        dialog.setContentText("Status:");

        // Traditional way to get the response value.
        return dialog.showAndWait().orElse(null);
    }

    private void updateStatusInDatabase(String vinNumber, String newStatus) {
        // Update the status in the database based on the VIN number
        int carId = getCarId(vinNumber);

        if (carId != -1) {
            String updateStatusSQL = "UPDATE appointments SET status = ? WHERE car_id = ?";

            try (Connection connection = DatabaseConnection.getConnection("postgres", "postgres", "admin");
                 PreparedStatement preparedStatement = connection.prepareStatement(updateStatusSQL)) {

                preparedStatement.setString(1, newStatus);
                preparedStatement.setInt(2, carId);

                int affectedRows = preparedStatement.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Status updated successfully");
                } else {
                    System.out.println("Failed to update status");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void logOutButtonOnAction() throws Exception {
        // Implement logic to log out
        changeScene(logOutButton, "login.fxml");
    }

    private void loadAllCars() {
        ObservableList<Car> cars = FXCollections.observableArrayList();

        String selectCarsSQL = "SELECT c.make, c.model, c.vin_number, a.notes AS problem, a.status " +
                "FROM cars c " +
                "JOIN appointments a ON c.car_id = a.car_id";

        try (Connection connection = DatabaseConnection.getConnection("postgres", "postgres", "admin");
             PreparedStatement preparedStatement = connection.prepareStatement(selectCarsSQL)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String make = resultSet.getString("make");
                String model = resultSet.getString("model");
                String vinNumber = resultSet.getString("vin_number");
                String problem = resultSet.getString("problem");
                String status = resultSet.getString("status");

                cars.add(new Car(make, model, vinNumber, problem, status));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        makeTableColumn.setCellValueFactory(cellData -> cellData.getValue().makeProperty());
        modelTableColumn.setCellValueFactory(cellData -> cellData.getValue().modelProperty());
        vinTableColumn.setCellValueFactory(cellData -> cellData.getValue().vinProperty());
        problemTableColumn.setCellValueFactory(cellData -> cellData.getValue().problemProperty());
        statusTableColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        carTableView.setItems(cars);
    }

    private int getCarId(String vinNumber) {
        String selectCarIdSQL = "SELECT car_id FROM cars WHERE vin_number = ?";

        try (Connection connection = DatabaseConnection.getConnection("postgres", "postgres", "admin");
             PreparedStatement preparedStatement = connection.prepareStatement(selectCarIdSQL)) {

            preparedStatement.setString(1, vinNumber);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("car_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return a default value (you might want to handle this differently based on your application logic)
        return -1;
    }
}
