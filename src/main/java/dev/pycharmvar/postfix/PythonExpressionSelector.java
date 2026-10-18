package dev.pycharmvar.postfix;

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateExpressionSelectorBase;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

final class PythonExpressionSelector extends PostfixTemplateExpressionSelectorBase {
    PythonExpressionSelector() {
        super(null);
    }

    @Override
    protected @NotNull List<PsiElement> getNonFilteredExpressions(
            @NotNull PsiElement context, @NotNull Document document, int offset) {
        return PythonExpressionResolver.findExpressions(context.getContainingFile(), offset);
    }
}
