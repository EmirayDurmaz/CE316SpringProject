package com.example.iae;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class ConfigDetailsController {
    @FXML private Label languageLabel;
    @FXML private Label compilerPathLabel;
    @FXML private Label compilerArgsLabel;
    @FXML private Label runCommandLabel;
    @FXML private Label expectedOutputLabel;

    @FXML private TableView<UserOutputScene> resultsTable;
    @FXML private TableColumn<UserOutputScene, String> studentIdColumn;
    @FXML private TableColumn<UserOutputScene, String> outputColumn;
    @FXML private TableColumn<UserOutputScene, String> expectedColumn;
    @FXML private TableColumn<UserOutputScene, String> resultColumn;

    private final ObservableList<UserOutputScene> resultsList = FXCollections.observableArrayList();

    public void loadConfigAndResults(String configFileName) {
        try {
            String configPath = "JSONFiles" + File.separator + configFileName;
            String configContent = new String(Files.readAllBytes(Paths.get(configPath)));
            JSONObject configJson = new JSONObject(configContent);
            JSONObject config = configJson.getJSONArray("Requirements").getJSONObject(0);

            languageLabel.setText(config.getString("Language"));
            compilerPathLabel.setText(config.getString("compilerPath"));
            compilerArgsLabel.setText(config.getString("compiler"));
            runCommandLabel.setText(config.getString("runCommand"));
            expectedOutputLabel.setText(config.getString("expected"));

            resultsList.clear();

            File resultsFile = new File("results.json");
            if (resultsFile.exists()) {
                String resultsContent = new String(Files.readAllBytes(resultsFile.toPath()));
                JSONArray resultsArray = new JSONArray(resultsContent);

                Set<String> seenStudentIds = new HashSet<>();

                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject obj = resultsArray.getJSONObject(i);

                    if (obj.optString("jsonFileName", "").equals(configFileName)) {
                        String fullPath = obj.getString("path");
                        String studentId = fullPath.contains("\\") ? fullPath.substring(fullPath.lastIndexOf("\\") + 1) : fullPath;

                        if (seenStudentIds.contains(studentId)) {
                            continue; // Aynı öğrenci zaten eklendi, atla
                        }
                        seenStudentIds.add(studentId);

                        String output = obj.getString("runOutput");
                        String expected = obj.getString("expectedOutput");
                        String result = obj.getString("result");

                        resultsList.add(new UserOutputScene(studentId, output, expected, result, "", "", "", ""));
                    }
                }
            }

            studentIdColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPath()));
            outputColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRunOutput()));
            expectedColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getExpectedOutput()));
            resultColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getResult()));

            resultColumn.setCellFactory(column -> new TableCell<UserOutputScene, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        setStyle("-fx-font-weight: bold;");
                        if (item.equalsIgnoreCase("correct")) {
                            setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-weight: bold;");
                        } else if (item.equalsIgnoreCase("incorrect")) {
                            setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-font-weight: bold;");
                        } else if (item.toLowerCase().contains("compile")) {
                            setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-font-weight: bold;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            });

            resultsTable.setItems(resultsList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @FXML
    public void handleClose() {
        Stage stage = (Stage) languageLabel.getScene().getWindow();
        stage.close();
    }
}
