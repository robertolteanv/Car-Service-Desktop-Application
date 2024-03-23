package org.example.cardealer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoggedInUser {

    @FXML
    private Button logOutUserButton;
    @FXML
    private Button editUserProfileButton;
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

    private SessionManager sessionManager;

    @FXML
    public void initialize() {
        sessionManager = SessionManager.getInstance();
        loadUserCars();
    }

    @FXML
    public void logOutButtonOnActions(ActionEvent e) {
        try {
            changeScene(logOutUserButton, "login.fxml");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void editUserProfileButtonOnActions() {
        try {
            changeScene(editUserProfileButton, "editProfile.fxml");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void changeScene(Button button, String fxmlFileName) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
        Parent root = loader.load();

        Stage stage = (Stage) button.getScene().getWindow();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    private void loadUserCars() {
        ObservableList<Car> cars = FXCollections.observableArrayList();

        String selectCarsSQL = "SELECT c.make, c.model, c.vin_number, a.notes AS problem, a.status " +
                "FROM cars c " +
                "JOIN appointments a ON c.car_id = a.car_id " +
                "WHERE c.user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection("postgres", "postgres", "admin");
             PreparedStatement preparedStatement = connection.prepareStatement(selectCarsSQL)) {

            preparedStatement.setInt(1, sessionManager.getUserId());
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
        vinTableColumn.setCellValueFactory(cellData -> cellData.getValue().vinNumberProperty());
        problemTableColumn.setCellValueFactory(cellData -> cellData.getValue().problemProperty());
        statusTableColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        carTableView.setItems(cars);
    }

}
