package com.example.iae;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.HashSet;
import java.util.Set;

public class UserOutputController implements Initializable {

    @FXML
    private TableView<UserOutputScene> resultsTable;

    @FXML
    private TableColumn<UserOutputScene, String> path;

    @FXML
    private TableColumn<UserOutputScene, String> expectedOutput;

    @FXML
    private TableColumn<UserOutputScene, String> output;

    @FXML
    private Button helpButton2;

    @FXML
    private TableColumn<UserOutputScene, String> result;

    private final ObservableList<UserOutputScene> resultsList = FXCollections.observableArrayList();
    private final Set<String> uniquePaths = new HashSet<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        path.setCellValueFactory(new PropertyValueFactory<>("path"));
        output.setCellValueFactory(new PropertyValueFactory<>("runOutput"));
        expectedOutput.setCellValueFactory(new PropertyValueFactory<>("expectedOutput"));
        result.setCellValueFactory(new PropertyValueFactory<>("result"));


        result.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold;");

                    if (item.equalsIgnoreCase("Correct")) {
                        setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-weight: bold;");
                    } else if (item.equalsIgnoreCase("Incorrect")) {
                        setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-font-weight: bold;");
                    } else if (item.toLowerCase().contains("compile")) {
                        setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-font-weight: bold;");
                    }
                }
            }
        });


        resultsTable.setItems(resultsList);

        helpButton2.setOnAction(actionEvent -> {
            String helpTXT = """
                    The "ID" column shows the student number.
                    The "OUTPUT" column shows the program's actual output.
                    The "EXPECTED OUTPUT" column shows what the output should be.
                    The "RESULT" column shows if the actual and expected outputs match:
                    ✔ If they match: "Correct"
                    ❌ If not: "Incorrect"
                    ⚠ Compilation issues appear in orange.
                    """;

            help(helpTXT, "Help");
        });

        showAllResults();
    }

    public static void help(String content, String header) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("HELP");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void addResult(String path, String output, String expectedOutput, String result) {
        for (UserOutputScene existing : resultsList) {
            if (existing.getPath().equals(path)) {
                return;
            }
        }
        resultsList.add(new UserOutputScene(path, output, expectedOutput, result));
    }

    public void showAllResults() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("results.json")));
            JSONArray jsonArray = new JSONArray(content);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String path = jsonObject.getString("path");
                String runOutput = jsonObject.getString("runOutput");
                String expectedOutput = jsonObject.getString("expectedOutput");
                String result = jsonObject.getString("result");

                addResult(path, runOutput, expectedOutput, result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
