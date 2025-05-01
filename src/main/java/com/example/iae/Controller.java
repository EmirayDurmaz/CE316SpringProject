package com.example.iae;

import com.example.iae.Compilers.CCompiler;
import com.example.iae.Compilers.CppCompiler;
import com.example.iae.Compilers.JavaCompiler;
import com.example.iae.Compilers.PythonCompiler;
import com.example.iae.Main;
import com.example.iae.Result;
import com.example.iae.UserOutputScene;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public Controller() {
    }

    @FXML
    private TextField pathtextField;
    @FXML
    private ChoiceBox<String> mychoiceBox;
    @FXML
    private ChoiceBox<String> savesChoiceBox;
    @FXML
    private Button refreshButton;
    @FXML
    private Button helpBtn;
    @FXML
    private Button okeyButton;
    @FXML
    private TextField compilerPathfield;
    @FXML
    private TextField compilerInterpreterargsfield;
    @FXML
    private TextField runcommandfield;
    @FXML
    private TextField expectedOutcomepathfield;
    @FXML
    private String[] languages = {"C", "C++", "Python", "JAVA"};

    public static String[] getFilenames(String directoryPath) {
        File directory = new File(directoryPath);

        if (!directory.exists() || !directory.isDirectory()) {
            return new String[0];
        }

        File[] files = directory.listFiles();

        String[] filenames = new String[files.length];

        for (int i = 0; i < files.length; i++) {
            filenames[i] = files[i].getName();
        }

        return filenames;
    }

    private String[] files;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        mychoiceBox.getItems().addAll(languages);
        mychoiceBox.getSelectionModel().selectFirst();


        files = getFilenames("JSONFiles");
        savesChoiceBox.getItems().clear();
        savesChoiceBox.getItems().addAll(files);
        if (files.length > 0) {
            savesChoiceBox.getSelectionModel().selectFirst();
        }


        refreshButton.setOnAction(actionEvent -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/createProject.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) refreshButton.getScene().getWindow();
                stage.setScene(scene);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        okeyButton.setOnAction(actionEvent -> {
            try {
                List<UserOutputScene> results = runButtonClicked();
                for (UserOutputScene result : results) {
                    int lastIndex = result.getPath().lastIndexOf("\\");
                    String path = result.getPath().substring(lastIndex + 1);
                    saveResultToJson(path, result.getRunOutput(), result.getExpectedOutput(), result.getResult());
                    Main.showResultScene(path, result.getRunOutput(), result.getExpectedOutput(), result.getResult());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public static void showHelp(String content, String header) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("HELP");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void pathDirectoryChooser(ActionEvent event) {
        Node source = (Node) event.getSource();
        Window window = source.getScene().getWindow();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(window);

        if (selectedDirectory != null) {
            String directoryPath = selectedDirectory.getAbsolutePath();
            pathtextField.setText(directoryPath);
            System.out.println("Selected directory path: " + directoryPath);
        } else {
            System.out.println("No directory selected");
        }
    }

    @FXML
    public void expectedDirectoryChooser(ActionEvent event) {
        Node source = (Node) event.getSource();
        Window window = source.getScene().getWindow();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(window);
        if (selectedDirectory != null) {
            String directoryPath = selectedDirectory.getAbsolutePath();
            expectedOutcomepathfield.setText(directoryPath);
            System.out.println("Selected directory path: " + directoryPath);
        } else {
            System.out.println("No directory selected");
        }
    }

    @FXML
    public void choiceBoxChanged(ActionEvent event) {
        String selectedLanguage = mychoiceBox.getSelectionModel().getSelectedItem();

        switch (selectedLanguage) {
            case "C":
                compilerPathfield.setText(CCompiler.COMPILER_PATH);
                compilerInterpreterargsfield.setText(CCompiler.ARGS);
                runcommandfield.setText(CCompiler.RUN_COMMAND);
                break;
            case "C++":
                compilerPathfield.setText(CppCompiler.COMPILER_PATH);
                compilerInterpreterargsfield.setText(CppCompiler.ARGS);
                runcommandfield.setText(CppCompiler.RUN_COMMAND);
                break;
            case "JAVA":
                compilerPathfield.setText(JavaCompiler.COMPILER_PATH);
                compilerInterpreterargsfield.setText(JavaCompiler.ARGS);
                runcommandfield.setText(JavaCompiler.RUN_COMMAND);
                break;
            case "Python":
                compilerPathfield.setText(PythonCompiler.COMPILER_PATH);
                compilerInterpreterargsfield.setText(PythonCompiler.ARGS);
                runcommandfield.setText("");
                break;
            default:
                break;
        }
    }

    @FXML
    public List<UserOutputScene> runButtonClicked() throws IOException {
        List<UserOutputScene> results = new ArrayList<>();
        String runOutput = null;
        String expectedOutput = null;
        String result = null;
        String path = pathtextField.getText();
        String expectedPath = expectedOutcomepathfield.getText();

        List<String> extractedFolders = new ArrayList<>();
        ZipExtractor zipExtractor = new ZipExtractor();
        extractedFolders = zipExtractor.extract(path);

        for (String folder : extractedFolders) {
            switch (mychoiceBox.getSelectionModel().getSelectedItem()) {
                case "C":
                    runOutput = compileAndRunC(adjustPath(path, folder));
                    expectedOutput = compileAndRunC(expectedPath);
                    break;
                case "C++":
                    runOutput = compileAndRunCpp(adjustPath(path, folder));
                    expectedOutput = compileAndRunCpp(expectedPath);
                    break;
                case "Python":
                    runOutput = runPythonInterpreter(adjustPath(path, folder));
                    expectedOutput = runPythonInterpreter(expectedPath);
                    break;
                case "JAVA":
                    runOutput = compileAndRunJava(adjustPath(path, folder));
                    expectedOutput = compileAndRunJava(expectedPath);
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported language selected");
            }

            result = runOutput.equals(expectedOutput) ? "Correct" : "Incorrect";
            results.add(new UserOutputScene(adjustPath(path, folder), runOutput, expectedOutput, result));
        }
        return results;
    }

    private String adjustPath(String path, String folder) {
        return path + "\\" + folder;
    }

    @FXML
    public String runPythonCompiler(String filePath) {
        File workingDirectory = new File(filePath);
        PythonCompiler pythonCompiler = new PythonCompiler(workingDirectory);

        try {
            Result runResult = pythonCompiler.run(compilerPathfield.getText(), compilerInterpreterargsfield.getText());
            return runResult.getOutput();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "-1";
    }

    @FXML
    public String compileAndRunJava(String filePath) {
        File workingDirectory = new File(filePath);
        JavaCompiler javaCompiler = new JavaCompiler(workingDirectory);

        try {
            Result compileResult = javaCompiler.compile(compilerPathfield.getText(), compilerInterpreterargsfield.getText());

            if (compileResult.getStatus() == 0) {
                Result runResult = javaCompiler.run(runcommandfield.getText(), "");
                return runResult.getOutput();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "-2";
    }

    @FXML
    public String compileAndRunC(String filePath) {
        File workingDirectory = new File(filePath);
        CCompiler cCompiler = new CCompiler(workingDirectory);

        try {
            Result compileResult = cCompiler.compile(compilerPathfield.getText(), compilerInterpreterargsfield.getText());

            if (compileResult.getStatus() == 0) {
                Result runResult = cCompiler.run(workingDirectory + runcommandfield.getText(), "");
                return runResult.getOutput();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "-3";
    }
