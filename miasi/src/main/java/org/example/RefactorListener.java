package org.example;

import grammar.JavaParser;
import grammar.JavaParserBaseListener;
import lombok.AllArgsConstructor;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStreamRewriter;
import java.util.regex.*;

@AllArgsConstructor
public class RefactorListener extends JavaParserBaseListener {

    private final TokenStreamRewriter rewriter;
    private final String oldName;
    private final String newName;

    private final String inputParam;

    public RefactorListener(CommonTokenStream tokens, String newName, String inputParam) {
        this.rewriter = new TokenStreamRewriter(tokens);
        this.oldName = inputParam.split("\\.").length == 2 ? inputParam.split("\\.")[1] : inputParam.split("\\.")[0] ;
        this.newName = newName;
        this.inputParam = inputParam;
    }

    @Override
    public void enterIdentifier(JavaParser.IdentifierContext ctx) {
        //System.out.println(oldName);
        ParserRuleContext parent = ctx.getParent();
        if (parent instanceof JavaParser.ClassDeclarationContext) {
            if (ctx.getText().equals(oldName) && isInsideClass(inputParam.split("\\.")[0], ctx)) {
                rewriter.replace(ctx.getStart(), newName);
            }
        } else if (parent instanceof JavaParser.MethodDeclarationContext) {
            if (ctx.getText().equals(oldName) && isInsideClass(inputParam.split("\\.")[0], ctx)) {
                rewriter.replace(ctx.getStart(), newName);
            }
        } else if (parent instanceof  JavaParser.MethodCallContext) {
            if (ctx.getText().equals(oldName) && isInsideClass(inputParam.split("\\.")[0], ctx)) {
                rewriter.replace(ctx.getStart(), newName);
            }
        } else if (parent instanceof JavaParser.CreatedNameContext) {
            if (ctx.getText().equals(oldName) && isInsideClass(inputParam.split("\\.")[0], ctx)) {
                rewriter.replace(ctx.getStart(), newName);
            }
        } else if (parent instanceof JavaParser.TypeIdentifierContext) {
            if (ctx.getText().equals(oldName) && isInsideClass(inputParam.split("\\.")[0], ctx)) {
                rewriter.replace(ctx.getStart(), newName);
            }
        } else if (parent instanceof JavaParser.VariableDeclaratorIdContext) {
            ParserRuleContext varDecl = parent.getParent(); // VariableDeclarator
            ParserRuleContext varDecls = varDecl != null ? varDecl.getParent() : null; // VariableDeclarators
            ParserRuleContext maybeField = varDecls != null ? varDecls.getParent() : null;
            if (maybeField instanceof JavaParser.FieldDeclarationContext && ctx.getText().equals(oldName) && isInsideClass(inputParam.split("\\.")[0], ctx)) {
                rewriter.replace(ctx.getStart(), newName);
            }
        } else if (parent instanceof JavaParser.ExpressionContext) {
            JavaParser.ExpressionContext exprCtx = (JavaParser.ExpressionContext) parent;
            if ((exprCtx.bop != null && ".".equals(exprCtx.bop.getText()) && isInsideClass(inputParam.split("\\.")[0], ctx))
                    && exprCtx.getChild(0).getText().equals("this")) {
                rewriter.replace(ctx.getStart(), newName);
            }
        }
    }

    @Override
    public void enterTypeIdentifier(JavaParser.TypeIdentifierContext ctx) {
        // typ zmiennej, np. MyClass obj;
        if (ctx.getText().equals(oldName)) {
            rewriter.replace(ctx.getStart(), newName);
        }
    }

    @Override
    public void enterCreatedName(JavaParser.CreatedNameContext ctx) {
        if (ctx.getText().equals(oldName)) {
            rewriter.replace(ctx.getStart(), newName);
        }
    }

    public String getRefactoredCode() {
        return rewriter.getText();
    }
    private boolean isInsideClass(String className, ParserRuleContext ctx) {
        while (ctx != null) {
            if (ctx instanceof JavaParser.ClassDeclarationContext classCtx) {
                // Próbujemy pobrać nazwę klasy
                String foundClassName = classCtx.getChild(1).getText(); // domyślnie Identifier
                return foundClassName.equals(className);
            }
            ctx = ctx.getParent();
        }
        return false;
    }


}
