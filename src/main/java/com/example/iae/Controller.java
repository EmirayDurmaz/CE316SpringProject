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
