package ru.edu.nsu.fit.martynov.autoparts;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class StoreController {
    // Login page
    @FXML
    private AnchorPane loginPage;

    @FXML private TextField loginPageLoginField;
    @FXML private PasswordField loginPagePasswordField;
    @FXML private Label loginPageErrorMessage;
    @FXML private ImageView loginPageImage;
    @FXML private Button loginPageSignInButton;

    @FXML
    protected void setLoginPageImage(Image image) {
        loginPageImage.setImage(image);
    }

    @FXML
    protected void setLoginPageErrorMessage(String message) {
        loginPageErrorMessage.setText(message);
    }

    @FXML
    protected void signIn() {
        if (AuthorizationManager.authorize(loginPageLoginField.getText().trim(), loginPagePasswordField.getText())) {
            loginPageLoginField.clear();
            loginPagePasswordField.clear();
            loginPageErrorMessage.setVisible(false);
            loginPage.setVisible(false);
            authorizedPage.setVisible(true);

        } else {
            incorrectPasswordLoginPage();
        }
    }

    @FXML
    protected void incorrectPasswordLoginPage() {
        loginPagePasswordField.clear();
        loginPagePasswordField.requestFocus();
        loginPageErrorMessage.setVisible(true);
    }

    // Authorized page
    @FXML
    TabPane authorizedPage;
}