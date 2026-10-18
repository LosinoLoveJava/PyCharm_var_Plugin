package dev.pycharmvar.postfix;

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public final class PythonVarPostfixTemplateProvider implements PostfixTemplateProvider {
    private final Set<PostfixTemplate> templates = Collections.singleton(new PythonVarPostfixTemplate(this));

    @Override
    public @NotNull Set<PostfixTemplate> getTemplates() {
        return templates;
    }

    @Override
    public boolean isTerminalSymbol(char currentChar) {
        return currentChar == '.';
    }

    @Override
    public void preExpand(@NotNull PsiFile file, @NotNull Editor editor) {
    }

    @Override
    public void afterExpand(@NotNull PsiFile file, @NotNull Editor editor) {
    }

    @Override
    public @NotNull PsiFile preCheck(@NotNull PsiFile copyFile, @NotNull Editor realEditor, int currentOffset) {
        return copyFile;
    }
}
