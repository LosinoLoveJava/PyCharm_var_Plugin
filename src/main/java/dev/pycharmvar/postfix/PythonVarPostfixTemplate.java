package dev.pycharmvar.postfix;

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateWithExpressionSelector;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/** The .var postfix entry that delegates the actual extraction to PyCharm. */
public final class PythonVarPostfixTemplate extends PostfixTemplateWithExpressionSelector {
    private static final String KEY = "var";

    public PythonVarPostfixTemplate(@NotNull PostfixTemplateProvider provider) {
        super("python.var", KEY, "name = expr", new PythonExpressionSelector(), provider);
    }

    @Override
    protected void prepareAndExpandForChooseExpression(@NotNull PsiElement expression, @NotNull Editor editor) {
        // Native refactoring manages its own commands and write actions.
        expandForChooseExpression(expression, editor);
    }

    @Override
    protected void expandForChooseExpression(@NotNull PsiElement expression, @NotNull Editor editor) {
        IntroduceVariableInvoker.invoke(editor, expression);
    }

    @Override
    public boolean isEditable() {
        return false;
    }
}
