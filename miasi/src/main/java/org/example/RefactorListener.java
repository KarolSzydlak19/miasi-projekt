package org.example;

import grammar.JavaParser;
import grammar.JavaParserBaseListener;
import lombok.AllArgsConstructor;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStreamRewriter;

import java.util.*;

@AllArgsConstructor
public class RefactorListener extends JavaParserBaseListener {

    private final TokenStreamRewriter rewriter;
    private final String oldName;
    private final String newName;

    private final String refactorType;

    private final String inputParam;
    private final boolean isLocalVariable;

    private final Map<ParserRuleContext, Set<String>> localVariables = new HashMap<>();
    private final Deque<ParserRuleContext> currentMethodContext = new ArrayDeque<>();

    public RefactorListener(CommonTokenStream tokens, String newName, String inputParam, String refactorType) {
        this.rewriter = new TokenStreamRewriter(tokens);
        this.oldName =  switch (inputParam.split("\\.").length) {
            case 1 ->  inputParam.split("\\.")[0];
            case 2 -> inputParam.split("\\.")[1];
            case 3 -> inputParam.split("\\.")[2];
            default -> null;
        };
        this.isLocalVariable = inputParam.split("\\.").length == 3;
        this.newName = newName;
        this.inputParam = inputParam;
        this.refactorType = refactorType;
    }

    /**
     * Metoda wykonuje refaktoryzację identyfikatorów (np. nazw klas, metod, zmiennych) w trakcie parsowania kodu Java.
     * Zmienia identyfikatory, które pasują do 'oldName' na 'newName' w odpowiednich kontekstach, w zależności od typu refaktoryzacji.
     *
     * Proces refaktoryzacji odbywa się na różnych poziomach struktury kodu, zależnie od tego, czy refaktoryzujemy:
     * - Nazwę klasy,
     * - Nazwę metody,
     * - Zmienną lokalną lub pole,
     * - Odwołanie do metody lub zmiennej.
     *
     * Zmieniany identyfikator jest zastępowany tylko wtedy, gdy spełnia warunki:
     * - Jest identyczny z 'oldName',
     * - Znajduje się wewnątrz klasy określonej w 'inputParam',
     * - Typ refaktoryzacji pasuje do aktualnego kontekstu (np. "class", "method", "variable").
     *
     * @param ctx Kontekst identyfikatora, który może być zmieniany.
     */
    @Override
    public void enterIdentifier(JavaParser.IdentifierContext ctx) {
        ParserRuleContext parent = ctx.getParent();
        if (parent instanceof JavaParser.ClassDeclarationContext && refactorType.equals("class")) {
            if (ctx.getText().equals(oldName) && isInsideClass(inputParam.split("\\.")[0], ctx)) {
                rewriter.replace(ctx.getStart(), newName);
            }
        } else if (parent instanceof JavaParser.MethodDeclarationContext && refactorType.equals("method")) {
            if (ctx.getText().equals(oldName) && isInsideClass(inputParam.split("\\.")[0], ctx)) {
                rewriter.replace(ctx.getStart(), newName);
            }
        } else if (parent instanceof  JavaParser.MethodCallContext && refactorType.equals("method")) {
            if (ctx.getText().equals(oldName) && isInsideClass(inputParam.split("\\.")[0], ctx)) {
                rewriter.replace(ctx.getStart(), newName);
            }
        } else if (parent instanceof JavaParser.CreatedNameContext && (refactorType.equals("variable") || refactorType.equals("object"))) {
            if (ctx.getText().equals(oldName) && isInsideClass(inputParam.split("\\.")[0], ctx)) {
                rewriter.replace(ctx.getStart(), newName);
            }
        } else if (parent instanceof JavaParser.TypeIdentifierContext) {
            if (ctx.getText().equals(oldName) && isInsideClass(inputParam.split("\\.")[0], ctx)) {
                rewriter.replace(ctx.getStart(), newName);
            }
        } else if (parent instanceof JavaParser.VariableDeclaratorIdContext && refactorType.equals("variable")) {   //pole klasy
            ParserRuleContext varDecl = parent.getParent(); // VariableDeclarator
            ParserRuleContext varDecls = varDecl != null ? varDecl.getParent() : null; // VariableDeclarators
            ParserRuleContext maybeField = varDecls != null ? varDecls.getParent() : null;
            if (maybeField instanceof JavaParser.FieldDeclarationContext && ctx.getText().equals(oldName) && isInsideClass(inputParam.split("\\.")[0], ctx) && !isLocalVariable) {
                rewriter.replace(ctx.getStart(), newName);
            }
        } else if (parent instanceof JavaParser.ExpressionContext) {
            JavaParser.ExpressionContext exprCtx = (JavaParser.ExpressionContext) parent;
            if ((exprCtx.bop != null && ".".equals(exprCtx.bop.getText()) && isInsideClass(inputParam.split("\\.")[0], ctx) && !isLocalVariable)
                    && exprCtx.getChild(0).getText().equals("this")) {
                rewriter.replace(ctx.getStart(), newName);
            }
        }
        else if (!currentMethodContext.isEmpty()) {
            ParserRuleContext methodCtx = currentMethodContext.peek();
            if (localVariables.get(methodCtx).contains(ctx.getText()) && ctx.getText().equals(oldName) && isInsideClass(inputParam.split("\\.")[0], ctx) && isInsideMethodInClass(inputParam.split("\\.")[1] ,ctx)) {
                rewriter.replace(ctx.getStart(), newName);
            }
        }
    }

    @Override
    public void enterTypeIdentifier(JavaParser.TypeIdentifierContext ctx) {
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

    @Override
    public void enterMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
        currentMethodContext.push(ctx);
        localVariables.put(ctx, new HashSet<>());
    }

    @Override
    public void exitMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
        currentMethodContext.pop();
    }

    @Override
    public void enterVariableDeclaratorId(JavaParser.VariableDeclaratorIdContext ctx) {
        if (!currentMethodContext.isEmpty()) {
            String varName = ctx.getText();
            ParserRuleContext currentMethod = currentMethodContext.peek();

            localVariables.get(currentMethod).add(varName);

            // Jeśli to ta zmienna, którą refaktorujemy - podmień
            if (varName.equals(oldName)  && isInsideClass(inputParam.split("\\.")[0], ctx) && isInsideMethodInClass(inputParam.split("\\.")[1] ,ctx)) {
                rewriter.replace(ctx.getStart(), newName);
            }
        }
    }

    public String getRefactoredCode() {
        return rewriter.getText();
    }
    private boolean isInsideClass(String className, ParserRuleContext ctx) {
        while (ctx != null) {
            if (ctx instanceof JavaParser.ClassDeclarationContext classCtx) {
                String foundClassName = classCtx.getChild(1).getText(); // domyślnie Identifier
                return foundClassName.equals(className);
            }
            ctx = ctx.getParent();
        }
        return false;
    }

    private boolean isInsideMethodInClass(String methodName, ParserRuleContext ctx) {
        while (ctx != null) {
            if (ctx instanceof JavaParser.MethodDeclarationContext classCtx) {
                String foundClassName = classCtx.getChild(1).getText(); // domyślnie Identifier
                return foundClassName.equals(methodName);
            }
            ctx = ctx.getParent();
        }
        return false;
    }
}
