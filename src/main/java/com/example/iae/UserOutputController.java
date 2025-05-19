package com.example.iae;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
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
    private Button backButton;

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

        exportButton.setOnAction(this::exportResults);

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

        backButton.setOnAction(actionEvent -> {
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.close();
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
        resultsTable.setRowFactory(tv -> {
            TableRow<UserOutputScene> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    UserOutputScene rowData = row.getItem();
                    showDetailsPopup(rowData);
                }
            });
            return row ;
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

    public void addResult(String path, String output, String expectedOutput, String result,
                          String language, String compilerPath, String compilerArgs, String runCommand) {
        for (UserOutputScene existing : resultsList) {
            if (existing.getPath().equals(path)) {
                return;
            }
        }
        resultsList.add(new UserOutputScene(path, output, expectedOutput, result, language, compilerPath, compilerArgs, runCommand));
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


                String language = jsonObject.optString("language", "");
                String compilerPath = jsonObject.optString("compilerPath", "");
                String compilerArgs = jsonObject.optString("compilerArgs", "");
                String runCommand = jsonObject.optString("runCommand", "");

                addResult(path, runOutput, expectedOutput, result, language, compilerPath, compilerArgs, runCommand);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private Button deleteStudentButton;

    @FXML
    public void deleteStudentResult(ActionEvent event) {
        UserOutputScene selected = resultsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            help("Please select a student result to delete.", "No Selection");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete the selected result?",
                ButtonType.YES, ButtonType.NO);

        alert.setTitle("Confirm Deletion");
        alert.setHeaderText(null);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                String studentPathToDelete = selected.getPath();

                JSONArray jsonArray = new JSONArray();
                try {
                    File file = new File("results.json");
                    if (file.exists()) {
                        String content = new String(Files.readAllBytes(file.toPath()));
                        jsonArray = new JSONArray(content);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                JSONArray newArray = new JSONArray();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String path = obj.optString("path", "");
                    if (!path.equals(studentPathToDelete)) {
                        newArray.put(obj);
                    }
                }

                try (FileWriter fileWriter = new FileWriter("results.json")) {
                    fileWriter.write(newArray.toString(4));
                    fileWriter.flush();
                    System.out.println("Student result deleted successfully.");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                resultsList.remove(selected);
            } else {
                System.out.println("Deletion cancelled by user.");
            }
        });
    }

    @FXML
    private Button exportButton;

    @FXML
    public void exportResults(ActionEvent event) {
        FileChooser directoryChooser = new FileChooser();


        DirectoryChooser directoryChooser2 = new DirectoryChooser();
        directoryChooser2.setTitle("Select Folder to Export Results");

        File defaultDirectory = new File(System.getProperty("user.home") + File.separator + "Desktop");
        directoryChooser2.setInitialDirectory(defaultDirectory);

        File selectedDirectory = directoryChooser2.showDialog(exportButton.getScene().getWindow());

        if (selectedDirectory != null) {
            File exportFolder = new File(selectedDirectory, "ExportedResults");
            if (!exportFolder.exists()) {
                boolean created = exportFolder.mkdir();
                if (!created) {
                    showAlert("Error", "Failed to create export folder.");
                    return;
                }
            }

            File sourceFile = new File("results.json");
            if (!sourceFile.exists()) {
                showAlert("Error", "results.json file does not exist.");
                return;
            }

            File destFile = new File(exportFolder, "results.json");

            try {
                Files.copy(sourceFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                showAlert("Success", "Results exported to:\n" + destFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to export results.");
            }
        } else {

            System.out.println("Export cancelled by user.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showDetailsPopup(UserOutputScene data) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Student Details");
        alert.setHeaderText("Details for: " + data.getPath());

        StringBuilder content = new StringBuilder();
        content.append("Language: ").append(data.getLanguage()).append("\n");
        content.append("Compiler Path: ").append(data.getCompilerPath()).append("\n");
        content.append("Compiler Arguments: ").append(data.getCompilerArgs()).append("\n");
        content.append("Run Command: ").append(data.getRunCommand()).append("\n");
        content.append("\n");
        content.append("Result: ").append(data.getResult()).append("\n");
        content.append("Output:\n").append(data.getRunOutput()).append("\n");
        content.append("Expected Output:\n").append(data.getExpectedOutput()).append("\n");

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

}