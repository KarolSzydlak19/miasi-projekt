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
        String inputCode = new String(Files.readAllBytes(Paths.get("miasi/src/main/resources/MyClass.java")));

        CharStream input = CharStreams.fromString(inputCode);
        JavaLexer lexer = new JavaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);

        ParseTree tree = parser.compilationUnit();

        RefactorListener listener = new RefactorListener(tokens, "newRewriter", "MyClass1.printField.myField");
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        System.out.println(listener.getRefactoredCode());
        saveToFile("miasi/src/main/resources/out.java", listener.getRefactoredCode());
    }

    public static void saveToFile(String filePath, String content) {
        try {
            Files.write(Paths.get(filePath), content.getBytes());
        } catch (IOException e) {

        }
    }
}