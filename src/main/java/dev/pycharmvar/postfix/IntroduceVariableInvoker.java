package dev.pycharmvar.postfix;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiDocumentManager;
import org.jetbrains.annotations.NotNull;

/** Invokes PyCharm's existing Introduce Variable action with the expression selected. */
final class IntroduceVariableInvoker {
    private static final String ACTION_ID = "IntroduceVariable";

    private IntroduceVariableInvoker() {
    }

    static void invoke(@NotNull Editor editor, @NotNull PsiElement expression) {
        Project project = expression.getProject();
        PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
        int start = expression.getTextRange().getStartOffset();
        int end = expression.getTextRange().getEndOffset();

        AnAction action = ActionManager.getInstance().getAction(ACTION_ID);
        if (action == null) {
            editor.getSelectionModel().removeSelection();
            HintManager.getInstance().showErrorHint(editor, "Introduce Variable is not available.");
            return;
        }

        long modificationStamp = editor.getDocument().getModificationStamp();
        Object commandGroup = new Object();
        CommandProcessor commands = CommandProcessor.getInstance();
        // Merge postfix removal and deferred extraction into one undo step.
        if (commands.getCurrentCommandName() != null) {
            commands.setCurrentCommandGroupId(commandGroup);
        }
        Runnable refactor = () -> {
            if (project.isDisposed() || editor.isDisposed()
                    || editor.getDocument().getModificationStamp() != modificationStamp) {
                return;
            }
            editor.getSelectionModel().setSelection(start, end);
            commands.executeCommand(project, () -> ActionManager.getInstance().tryToExecute(
                    action, null, editor.getContentComponent(), "PyCharmVarPostfix", true),
                    "Introduce Variable", commandGroup);
        };

        // Tab's outer editor action may still hold a write lock, even though the
        // postfix template itself requests no write action. Native UI needs EDT without that lock.
        if (ApplicationManager.getApplication().isWriteAccessAllowed()) {
            ApplicationManager.getApplication().invokeLater(refactor, ModalityState.defaultModalityState());
        } else {
            refactor.run();
        }
    }
}
