package com.example.iae;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    private static final String APP_TITLE = "Integrated Environment System";
    private static final String CSS_PATH = "style.css";

    @Override
    public void start(Stage primaryStage) throws IOException {
        URL fxmlLocation = getClass().getResource("scene1.fxml");
        checkResourceExists(fxmlLocation, "scene1.fxml");

        Parent root = FXMLLoader.load(fxmlLocation);
        setupPrimaryStage(primaryStage, root);
    }

    private void setupPrimaryStage(Stage stage, Parent root) {
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource(CSS_PATH).toExternalForm());
        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
        stage.show();
    }

    public static void showCreateProject() throws IOException {
        createAndShowModalWindow("createProject.fxml", "Create Project");
    }

    public static void showResultScene(String filePath, String output,
                                       String expectedOutput, String result) throws IOException {
        FXMLLoader loader = loadFXML("resultScene.fxml");
        AnchorPane resultScene = loader.load();
        UserOutputController controller = loader.getController();
        controller.addResult(filePath, output, expectedOutput, result);

        Stage stage = createModalStage("Result", resultScene);
        stage.showAndWait();
    }

    private static void createAndShowModalWindow(String fxmlFile, String title) throws IOException {
        FXMLLoader loader = loadFXML(fxmlFile);
        Parent root = loader.load();
        Stage stage = createModalStage(title, root);
        stage.showAndWait();
    }

    private static FXMLLoader loadFXML(String fxmlFile) throws IOException {
        URL fxmlLocation = Main.class.getResource(fxmlFile);
        checkResourceExists(fxmlLocation, fxmlFile);
        return new FXMLLoader(fxmlLocation);
    }

    private static Stage createModalStage(String title, Parent root) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.WINDOW_MODAL);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Main.class.getResource(CSS_PATH).toExternalForm());
        stage.setScene(scene);

        return stage;
    }

    private static void checkResourceExists(URL resource, String resourceName) {
        if (resource == null) {
            System.err.println(resourceName + " not found!");
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}