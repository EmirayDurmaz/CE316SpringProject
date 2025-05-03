package com.example.iae;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private Button createNewButton;
    @FXML
    private Button helpButton;
    @FXML
    private Button exitButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        createNewButton.getStyleClass().add("button-green");
        helpButton.getStyleClass().add("button");
        exitButton.getStyleClass().add("button-red");

        createNewButton.setOnAction(actionEvent -> {
            try {
                Main.showCreateProject();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "An error occurred while trying to create a new project.");
            }
        });

        helpButton.setOnAction(e -> createHelpWindow());
        exitButton.setOnAction(actionEvent -> Platform.exit());
    }

    public static void createHelpWindow() {
        Stage helpStage = new Stage();
        helpStage.setTitle("Help Center");

        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("help-tab-pane");

        // Sekmeleri oluştur
        Tab manualTab = createHelpTab("📖 User Manual", "red", "manual.txt", "#d3f9d8");
        Tab faqTab = createHelpTab("❓ FAQ", "blue", "faq.txt", "#f9d8d8");
        Tab basicTab = createHelpTab("ℹ️ Basic Help", "green", "basic.txt", "#d8e4f9");

        tabPane.getTabs().addAll(manualTab, faqTab, basicTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Scene scene = new Scene(tabPane, 500, 400);
        scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());
        helpStage.setScene(scene);
        helpStage.setResizable(true);
        helpStage.show();
    }

    private static Tab createHelpTab(String title, String color, String fileName, String bgColor) {
        Label label = new Label(title);
        label.setStyle("-fx-text-fill: white; -fx-background-color: " + color + "; -fx-padding: 5px; -fx-font-size: 18px; -fx-font-weight: bold;");

        TextArea textArea = new TextArea(loadHelpText(fileName));
        textArea.setWrapText(true);
        textArea.setEditable(false);
        textArea.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: black; -fx-font-size: 14px; -fx-padding: 10px;");

        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Tab tab = new Tab();
        tab.setGraphic(label);
        tab.setContent(scrollPane);
        return tab;
    }

    private static String loadHelpText(String fileName) {
        try (InputStream inputStream = Main.class.getResourceAsStream("/help/" + fileName)) {
            if (inputStream == null) return "File not found: " + fileName;
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "Error reading help file: " + e.getMessage();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}