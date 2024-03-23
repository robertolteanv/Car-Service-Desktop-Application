package org.example.cardealer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditProfileController {
    @FXML
    private Button goBackButton;

    @FXML
    private PasswordField currentPassword;

    @FXML
    private TextField changeEmail;

    @FXML
    private TextField changeUsername;

    @FXML
    private PasswordField changePassword;

    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String JDBC_USER = "postgres";
    private static final String JDBC_PASSWORD = "admin";

    public void updateProfileInfo(ActionEvent actionEvent) {
        SessionManager sessionManager = SessionManager.getInstance();
        int userId = sessionManager.getUserId();

        String currentPassword1 = currentPassword.getText();
        String newEmail = changeEmail.getText();
        String newUsername = changeUsername.getText();
        String newPassword = changePassword.getText();

        if (updateProfileInfoInDatabase(userId, currentPassword1, newEmail, newUsername, newPassword)) {
            System.out.println("Profile information updated successfully!");
        } else {
            System.out.println("Failed to update profile information.");
        }
    }

    private boolean updateProfileInfoInDatabase(int userId, String currentPassword, String newEmail, String newUsername, String newPassword) {
        if (!isCurrentPasswordCorrect(userId, currentPassword)) {
            System.out.println("Invalid current password. Update failed.");
            return false;
        }

        String updateProfileInfoSQL = "UPDATE users SET email = ?, username = ?, password_hash = ? WHERE user_id = ?";

        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(updateProfileInfoSQL)) {

            preparedStatement.setString(1, newEmail);
            preparedStatement.setString(2, newUsername);
            preparedStatement.setString(3, newPassword);
            preparedStatement.setInt(4, userId);

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                return true;
            } else {
                System.out.println("No rows were updated. User not found?");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean isCurrentPasswordCorrect(int userId, String currentPassword) {
        String selectPasswordSQL = "SELECT password_hash FROM users WHERE user_id = ?";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(selectPasswordSQL)) {

            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password_hash");
                return storedPassword.equals(currentPassword);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void changeScene(Button button, String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent root = loader.load();

            Stage stage = (Stage) button.getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void goBackButtonOnActions() {
        changeScene(goBackButton, "userLoggedin.fxml");
    }
}
