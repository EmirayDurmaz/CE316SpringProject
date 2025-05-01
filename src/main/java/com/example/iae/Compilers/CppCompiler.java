package com.example.iae.Compilers;
import java.io.File;
import com.example.iae.Result;
public class CppCompiler extends Compiler {
    public static final String COMPILER_PATH = "g++";
    public static final String ARGS = "hello.cpp -o hello";
    public static final String RUN_COMMAND = "/hello.exe";
    public CppCompiler(File workingDirectory) {
        super(workingDirectory);
    }
}
