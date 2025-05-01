package com.example.iae;

public class UserOutputScene {
    private String path;
    private String expectedOutput;
    private String runOutput;
    private String result;

    public UserOutputScene(String path, String runOutput, String expectedOutput, String result) {
        this.expectedOutput = expectedOutput;
        this.runOutput = runOutput;
        this.result = result;
        this.path = path;
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
}

