package dev.pycharmvar.postfix;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyStatement;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.PyImportElement;

import java.util.ArrayList;
import java.util.List;

final class PythonExpressionResolver {
    private PythonExpressionResolver() {
    }

    static List<PsiElement> findExpressions(PsiFile file, int offset) {
        List<PsiElement> expressions = new ArrayList<>();
        if (!(file instanceof PyFile) || offset <= 0 || offset > file.getTextLength()) {
            return expressions;
        }

        PsiElement leaf = file.findElementAt(offset - 1);
        if (leaf == null
                || PsiTreeUtil.getNonStrictParentOfType(leaf, PsiComment.class) != null
                || PsiTreeUtil.getNonStrictParentOfType(leaf, PyImportElement.class) != null) {
            return expressions;
        }

        // Only expressions ending at the postfix boundary can be extracted.
        // A suffix typed inside a string is therefore not a candidate.
        for (PsiElement element = leaf; element != null && !(element instanceof PyStatement);
             element = element.getParent()) {
            if (element instanceof PyExpression
                    && !(element instanceof PyTargetExpression)
                    && element.getTextRange().getEndOffset() == offset
                    && !PsiTreeUtil.hasErrorElements(element)) {
                expressions.add(element);
            }
        }
        return expressions;
    }
}
