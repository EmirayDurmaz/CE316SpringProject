package com.example.iae;

public class UserOutputScene {
    private String path;
    private String expectedOutput;
    private String runOutput;
    private String result;
    private String language;
    private String compilerPath;
    private String compilerArgs;
    private String runCommand;

    public UserOutputScene(String path, String runOutput, String expectedOutput, String result, String language, String compilerPath, String compilerArgs, String runCommand) {
        this.path = path;
        this.runOutput = runOutput;
        this.expectedOutput = expectedOutput;
        this.result = result;
        this.language = language;
        this.compilerPath = compilerPath;
        this.compilerArgs = compilerArgs;
        this.runCommand = runCommand;
    }



    public String getExpectedOutput() {
        return expectedOutput;
    }

    public String getRunOutput() {
        return runOutput;
    }

    public String getResult() {
        return result;
    }

    public String getPath() {
        return path;
    }
    public String getLanguage() { return language; }
    public String getCompilerPath() { return compilerPath; }
    public String getCompilerArgs() { return compilerArgs; }
    public String getRunCommand() { return runCommand; }

}

