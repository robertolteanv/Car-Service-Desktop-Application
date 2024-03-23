package org.example.cardealer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;



public class AddCarController {
    @FXML
    private TextField usernameField;

    @FXML
    private TextField makeField;

    @FXML
    private TextField modelField;

    @FXML
    private TextField yearField;

    @FXML
    private TextField vinField;

    @FXML
    private TextField licensePlateField;

    @FXML
    private TextArea notesArea;

    @FXML
    private ChoiceBox<String> statusChoiceBox;

    @FXML
    private Button addButton;
    @FXML
    private ChoiceBox<String> notesChoiceBox;


    private void initializeServicesDropdown() {
        List<String> availableServices = DatabaseConnection.getAvailableServices();
        notesChoiceBox.getItems().addAll(availableServices);
    }

    @FXML
    private void initialize() {
        // Initialize the choice box with status options
        statusChoiceBox.getItems().addAll("Scheduled", "In Progress", "Completed");
        initializeServicesDropdown();

    }
    @FXML
    private void closeButtonOnAction() {
        // Close the add car window
        ((Stage) addButton.getScene().getWindow()).close();
    }

    @FXML
    private void addButtonOnAction() {
        // Get values from fields
        String username = usernameField.getText();
        String make = makeField.getText();
        String model = modelField.getText();
        int year = Integer.parseInt(yearField.getText());
        String vin = vinField.getText();
        String licensePlate = licensePlateField.getText();
        String selectedNote = notesChoiceBox.getValue();
        String status = statusChoiceBox.getValue();

        // Insert data into the database
        insertDataIntoDatabase(username, make, model, year, vin, licensePlate, selectedNote, status);

    }

    private void insertDataIntoDatabase(String username, String make, String model, int year,
                                        String vin, String licensePlate, String notes, String status) {

        String selectUserSQL = "SELECT user_id FROM users WHERE username = ?";
        String insertCarSQL = "INSERT INTO cars (user_id, make, model, year, vin_number, license_plate) VALUES (?, ?, ?, ?, ?, ?)";
        String insertAppointmentSQL = "INSERT INTO appointments (car_id, user_id, notes, status) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection("postgres", "postgres", "admin")) {
            // Get the user_id based on the username
            int userId = getUserId(connection, selectUserSQL, username);

            // Insert data into the cars table
            try (PreparedStatement carStatement = connection.prepareStatement(insertCarSQL, Statement.RETURN_GENERATED_KEYS)) {
                carStatement.setInt(1, userId);
                carStatement.setString(2, make);
                carStatement.setString(3, model);
                carStatement.setInt(4, year);
                carStatement.setString(5, vin);
                carStatement.setString(6, licensePlate);
                carStatement.executeUpdate();

                // Retrieve the generated car_id
                try (ResultSet generatedKeys = carStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int carId = ((ResultSet) generatedKeys).getInt(1);

                        // Insert data into the appointments table
                        try (PreparedStatement appointmentStatement = connection.prepareStatement(insertAppointmentSQL)) {
                            appointmentStatement.setInt(1, carId);
                            appointmentStatement.setInt(2, userId);
                            appointmentStatement.setString(3, notes);
                            appointmentStatement.setString(4, status);
                            appointmentStatement.executeUpdate();
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getUserId(Connection connection, String selectUserSQL, String username) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectUserSQL)) {
            preparedStatement.setString(1, username);
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("user_id");
                }
            }
        }
        return -1;
    }


    private int getCarId(Connection connection, String vin) throws SQLException {
        String selectCarIdSQL = "SELECT car_id FROM cars WHERE vin_number = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectCarIdSQL)) {
            preparedStatement.setString(1, vin);
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("car_id");
                }
            }
        }
        return -1;
    }


}
