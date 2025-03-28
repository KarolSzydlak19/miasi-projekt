package org.example;

import grammar.JavaParser;
import grammar.JavaParserBaseVisitor;
import lombok.AllArgsConstructor;
import org.antlr.v4.runtime.TokenStreamRewriter;

@AllArgsConstructor
public class RefactorVisitor extends JavaParserBaseVisitor<String> {

    private final TokenStreamRewriter rewriter;
    private final String oldName;
    private final String newName;

    @Override
    public String visitCompilationUnit(JavaParser.CompilationUnitContext ctx) {
        visitChildren(ctx);
        return rewriter.getText();
    }

    @Override
    public String visitVariableDeclaratorId(JavaParser.VariableDeclaratorIdContext ctx) {
        if (ctx.getText().equals(oldName)) {
            rewriter.replace(ctx.start, newName);
        }
        return null;
    }

    @Override
    public String visitExpression(JavaParser.ExpressionContext ctx) {
        if (ctx.getText().equals(oldName)) {
            rewriter.replace(ctx.start, newName);
        }
        return null;
    }

}