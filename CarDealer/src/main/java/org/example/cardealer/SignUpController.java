package org.example.cardealer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SignUpController {
    @FXML
    private TextField usernameFieldSU;
    @FXML
    private PasswordField passwordFieldSU;
    @FXML
    private PasswordField passwordFieldSU2;
    @FXML
    private Button exitButtonSU;
    @FXML
    private Button signUpButtonSU;
    @FXML
    private TextField emailFieldSU;
    @FXML
    private Label signupMessageLabel;
    @FXML
    private Button goBackButton;

    public void exitButtonSuOnAction2(ActionEvent e) {
        Stage stage = (Stage) exitButtonSU.getScene().getWindow();
        stage.close();
    }

    private boolean verifyPasswordsMatch() {
        String password = passwordFieldSU.getText();
        String reenteredPassword = passwordFieldSU2.getText();

        return password.equals(reenteredPassword);
    }

    public void signUpButtonSuOnActions(ActionEvent e) {
        if (verifyInputs()) {
            validateSignUp();
        } else {
            signupMessageLabel.setText("Please fill in all fields correctly.");
        }
    }

    private boolean verifyInputs() {
        return !usernameFieldSU.getText().isBlank() &&
                !passwordFieldSU.getText().isBlank() &&
                !passwordFieldSU2.getText().isBlank() &&
                !emailFieldSU.getText().isBlank() &&
                verifyPasswordsMatch();
    }

    // TODO: Add proper exception handling and hashing for password security
    public void validateSignUp() {
        String dbname = "postgres";
        String pass = "admin";
        String user = "postgres";
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection(dbname, user, pass);

        String selectUsernameQuery = "SELECT count(1) FROM users WHERE username = ?";
        String insertUserQuery = "INSERT INTO users(username, password_hash, email, user_type) VALUES (?, ?, ?, 'customer')";

        try {
            // Check if the username already exists
            PreparedStatement usernameStatement = connectDB.prepareStatement(selectUsernameQuery);
            usernameStatement.setString(1, usernameFieldSU.getText());
            ResultSet usernameResult = usernameStatement.executeQuery();

            if (usernameResult.next() && usernameResult.getInt(1) == 0) {
                // Username is available, proceed with registration
                PreparedStatement insertUserStatement = connectDB.prepareStatement(insertUserQuery);
                insertUserStatement.setString(1, usernameFieldSU.getText());
                // TODO: Hash the password using BCrypt or another secure hashing method
                insertUserStatement.setString(2, passwordFieldSU.getText());
                insertUserStatement.setString(3, emailFieldSU.getText());

                int rowsAffected = insertUserStatement.executeUpdate();
                if (rowsAffected > 0) {
                    signupMessageLabel.setText("User has been registered successfully!");
                } else {
                    signupMessageLabel.setText("Registration failed. Please try again.");
                }

                insertUserStatement.close();
            } else {
                signupMessageLabel.setText("Username is already taken. Please choose another.");
            }

            usernameStatement.close();
            usernameResult.close();
            connectDB.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void goBackButtonOnActions(ActionEvent e) throws Exception {
        changeScene(goBackButton, "login.fxml");
    }

    private void changeScene(Button button, String fxmlFileName) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
        Parent root = loader.load();

        Stage stage = (Stage) button.getScene().getWindow();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }
}
