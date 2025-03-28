package org.example;

import grammar.JavaLexer;
import grammar.JavaParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
    public static void main(String[] args) throws Exception {
        // create a CharStream that reads from standard input
        //CharStream input = CharStreams.fromStream(System.in);
        CharStream input = CharStreams.fromFileName("Main-copy.java");

        // create a lexer that feeds off of input CharStream
        JavaLexer lexer = new JavaLexer(input);

        // create a buffer of tokens pulled from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // create a parser that feeds off the tokens buffer
        JavaParser parser = new JavaParser(tokens);

        // start parsing at the main rule
        ParseTree tree = parser.compilationUnit();
        // System.out.println(tree.toStringTree(parser));

        // create a visitor to traverse the parse tree
        RefactorVisitor visitor = new RefactorVisitor(tokens, "input", "qwerty");
        System.out.println(visitor.visit(tree));
    }
}