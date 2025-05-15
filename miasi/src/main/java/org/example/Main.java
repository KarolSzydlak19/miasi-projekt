package org.example;

import grammar.JavaLexer;
import grammar.JavaParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println("Usage: java -jar yourapp.jar <sourceFile> <newName> <inputParam> <refactorType>");
            System.exit(1);
        }

        String sourceFile = args[0];     // e.g., src/main/resources/MyClass.java
        String newName = args[1];        // e.g., newField
        String inputParam = args[2];     // e.g., MyClass1.myField
        String refactorType = args[3];   // e.g., field / method / constructor
        String fileDestination = args[4]; // src/main/resources/out.java

        String inputCode = new String(Files.readAllBytes(Paths.get(sourceFile)));

        CharStream input = CharStreams.fromString(inputCode);
        JavaLexer lexer = new JavaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);

        ParseTree tree = parser.compilationUnit();

        RefactorListener listener = new RefactorListener(tokens, newName, inputParam, refactorType);
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        saveToFile(fileDestination, listener.getRefactoredCode());
    }

    public static void saveToFile(String filePath, String content) {
        try {
            Files.write(Paths.get(filePath), content.getBytes());
        } catch (IOException e) {
            System.err.println("Error writing to file: " + filePath);
            e.printStackTrace();
        }
    }
}
