package com.example.iae.Compilers;
import com.example.iae.Result;

import java.io.File;
public class PythonCompiler extends Compiler {

    public static final String COMPILER_PATH = "python";
    public static final String ARGS = "PythonTest.py";

    public PythonCompiler(File workingDirectory) {
        super(workingDirectory);
    }

    @Override
    public Result compile(String path, String args) throws Exception {
        return super.compile(path, args);
    }


}