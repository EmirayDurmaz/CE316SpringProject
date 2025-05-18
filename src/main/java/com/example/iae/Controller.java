package com.example.iae;

import com.example.iae.Compilers.CCompiler;
import com.example.iae.Compilers.CppCompiler;
import com.example.iae.Compilers.JavaCompiler;
import com.example.iae.Compilers.PythonCompiler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
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
        File folder = new File("JSONFiles");
        if (folder.exists() && folder.isDirectory()) {
            File[] filesInFolder = folder.listFiles();
            if (filesInFolder != null) {
                for (File file : filesInFolder) {
                    if (file.isFile() && file.getName().endsWith(".json")) {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            System.out.println("Could not delete file: " + file.getName());
                        }
                    }
                }
            }
        }

        mychoiceBox.getItems().addAll(languages);
        mychoiceBox.getSelectionModel().selectFirst();

        savesChoiceBox.getItems().clear();

        pathtextField.clear();
        compilerPathfield.clear();
        compilerInterpreterargsfield.clear();
        runcommandfield.clear();
        expectedOutcomepathfield.clear();

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
                String[] parts = CppCompiler.ARGS.split("-o");
                if (parts.length == 2) {
                    String exeName = parts[1].trim();
                    runcommandfield.setText(exeName.endsWith(".exe") ? exeName : exeName + ".exe");
                } else {
                    runcommandfield.setText("");
                }
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
                    runOutput = runPythonCompiler(adjustPath(path, folder));
                    expectedOutput = runPythonCompiler(expectedPath);
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
            System.out.println("Run Output: " + runResult.getOutput());
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
            File[] javaFiles = workingDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".java"));
            if (javaFiles == null || javaFiles.length == 0) {
                return "Compile error: No .java file found in " + filePath;
            }

            String javaFile = javaFiles[0].getName();
            String className = javaFile.replace(".java", "");

            Result compileResult = javaCompiler.compile("javac", javaFile);
            if (compileResult.getStatus() != 0) {
                return "Compile error:\n" + compileResult.getError();
            }

            Result runResult = javaCompiler.run("java " + className, "");
            return runResult.getOutput();

        } catch (Exception e) {
            e.printStackTrace();
            return "Exception: " + e.getMessage();
        }
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
    @FXML
    public String compileAndRunCpp(String filePath) {
        File workingDirectory = new File(filePath);
        CppCompiler cppCompiler = new CppCompiler(workingDirectory);

        try {
            File[] cppFiles = workingDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".cpp"));
            if (cppFiles == null || cppFiles.length == 0) {
                return "Compile error: No .cpp file found in " + filePath;
            }

            String cppFile = cppFiles[0].getName();
            String exeName = cppFile.replace(".cpp", ".exe");

            Result compileResult = cppCompiler.compile("g++", cppFile + " -o " + exeName);
            if (compileResult.getStatus() != 0) {
                System.err.println("❌ Compilation Error\n" + compileResult.getError());
                return "Compile error:\n" + compileResult.getError();
            }

            File exeFile = new File(workingDirectory, exeName);
            Result runResult = cppCompiler.run(exeFile.getAbsolutePath(), "");

            return runResult.getOutput();

        } catch (Exception e) {
            e.printStackTrace();
            return "Exception: " + e.getMessage();
        }
    }

    public void json() {
        String folderPath = "JSONFiles";
        File directory = new File(folderPath);

        if (!directory.exists()) {
            directory.mkdir();
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String jsonFileName = "JsonFile_" + System.currentTimeMillis() + ".json";
            String jsonFilePath = folderPath + File.separator + jsonFileName;

            JsonNode jsonData = objectMapper.createObjectNode();

            JsonNode data = objectMapper.createObjectNode()
                    .put("Language", mychoiceBox.getSelectionModel().getSelectedItem())
                    .put("chooseFile", pathtextField.getText())
                    .put("compilerPath", compilerPathfield.getText())
                    .put("compiler", compilerInterpreterargsfield.getText())
                    .put("runCommand", runcommandfield.getText())
                    .put("expected", expectedOutcomepathfield.getText());

            List<JsonNode> infos = new ArrayList<>();
            infos.add(data);

            ((ObjectNode) jsonData).set("Requirements", objectMapper.valueToTree(infos));

            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(new File(jsonFilePath), jsonData);

            System.out.println("Added to JSON: " + data.toString());


            savesChoiceBox.getItems().clear();
            savesChoiceBox.getItems().addAll(getFilenames("JSONFiles"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveResultToJson(String path, String runOutput, String expectedOutput, String result) {
        String fileName = "results.json";
        JSONArray jsonArray = new JSONArray();
        try {
            File file = new File(fileName);
            if (file.exists()) {
                String content = new String(Files.readAllBytes(file.toPath()));
                jsonArray = new JSONArray(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("path", path);
        jsonObject.put("runOutput", runOutput);
        jsonObject.put("expectedOutput", expectedOutput);
        jsonObject.put("result", result);
        jsonArray.put(jsonObject);

        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(jsonArray.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void loadSelectedJson(ActionEvent event) {
        String selectedJsonFileName = savesChoiceBox.getSelectionModel().getSelectedItem();
        String selectedJsonFilePath = "JSONFiles" + File.separator + selectedJsonFileName;

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(new File(selectedJsonFilePath));

            String language = rootNode.path("Requirements").path(0).path("Language").asText();
            String chooseFile = rootNode.path("Requirements").path(0).path("chooseFile").asText();
            String compilerPath = rootNode.path("Requirements").path(0).path("compilerPath").asText();
            String interpreterArgs = rootNode.path("Requirements").path(0).path("compiler").asText();
            String runCommand = rootNode.path("Requirements").path(0).path("runCommand").asText();
            String expected = rootNode.path("Requirements").path(0).path("expected").asText();

            mychoiceBox.getSelectionModel().select(language);
            pathtextField.setText(chooseFile);
            compilerPathfield.setText(compilerPath);
            compilerInterpreterargsfield.setText(interpreterArgs);
            runcommandfield.setText(runCommand);
            expectedOutcomepathfield.setText(expected);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void clearJson() {
        String selectedJsonFileName = savesChoiceBox.getSelectionModel().getSelectedItem();

        if (selectedJsonFileName == null || selectedJsonFileName.isEmpty()) {
            showHelp("Please select a configuration file to delete.", "No File Selected");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete the selected configuration?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Configuration");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                String selectedJsonFilePath = "JSONFiles" + File.separator + selectedJsonFileName;
                File file = new File(selectedJsonFilePath);

                if (file.exists()) {
                    boolean deleted = file.delete();
                    if (deleted) {
                        System.out.println("Selected JSON file has been deleted successfully.");

                        savesChoiceBox.getItems().remove(selectedJsonFileName);
                        if (!savesChoiceBox.getItems().isEmpty()) {
                            savesChoiceBox.getSelectionModel().selectFirst();
                        } else {
                            pathtextField.clear();
                            compilerPathfield.clear();
                            compilerInterpreterargsfield.clear();
                            runcommandfield.clear();
                            expectedOutcomepathfield.clear();
                            mychoiceBox.getSelectionModel().selectFirst();
                        }

                    } else {
                        showHelp("Failed to delete the selected JSON file.", "Delete Error");
                    }
                } else {
                    showHelp("The selected file does not exist.", "File Not Found");
                }
            }
        });
    }


    private static final String FILE_NAME = "results.json";

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            deleteResultsFile();
        }));
    }


    public static void deleteResultsFile() {
        try {
            File file = new File(FILE_NAME);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    System.out.println("results.json successfully deleted!");
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}