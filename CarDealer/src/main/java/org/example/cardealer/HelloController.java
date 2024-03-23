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

public class HelloController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button exitButton;
    @FXML
    private Button loginButton;
    @FXML
    private Label loginMessageLabel;
    @FXML
    private Button signupButton;
    UserRole userRole=UserRole.CUSTOMER;

    public void exitButtonOnActions(ActionEvent e) {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }

    public void loginButtonOnActions(ActionEvent e) {
        if (!usernameField.getText().isBlank() && !passwordField.getText().isBlank()) {
            validateLogin();
        } else {
            loginMessageLabel.setText("Please enter username and password");
        }
    }

    public void signUpButtonOnActions(ActionEvent e) {
        try {
            changeScene(signupButton, "signup.fxml");
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

    public void validateLogin() {
        String dbname = "postgres";
        String pass = "admin";
        String user = "postgres";
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection(dbname, user, pass);

        // Use a prepared statement to avoid SQL injection
        String verifyLogin = "SELECT user_id, username, user_type FROM users WHERE username = ? AND password_hash = ?";

        try {
            PreparedStatement preparedStatement = connectDB.prepareStatement(verifyLogin);
            preparedStatement.setString(1, usernameField.getText());
            preparedStatement.setString(2, passwordField.getText());

            ResultSet queryResult = preparedStatement.executeQuery();
            if (queryResult.next()) {
                // Retrieve user information
                int userId = queryResult.getInt("user_id");
                String username = queryResult.getString("username");
                String userType = queryResult.getString("user_type");

                // Inside the LoginController after successful authentication
                SessionManager sessionManager = SessionManager.getInstance();
                sessionManager.setUserId(userId);
                sessionManager.setUsername(username);
                sessionManager.setUserType(userType);
                // Add more session information as needed

                // Redirect based on user type
                if ("mechanic".equals(userType)) {
                    changeScene(loginButton, "mechanicLoggedIn.fxml");
                    userRole=UserRole.MECHANIC;
                } else {
                    changeScene(loginButton, "userLoggedin.fxml");
                }
            } else {
                loginMessageLabel.setText("Invalid login. Please try again.");
            }

            preparedStatement.close();
            queryResult.close();
            connectDB.close();
            System.out.println("Login Successful");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
