package dev.pycharmvar.postfix;

import com.intellij.codeInsight.template.CustomTemplateCallback;
import com.intellij.codeInsight.template.postfix.templates.LanguagePostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixLiveTemplate;
import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.template.impl.TemplateState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.ServiceContainerUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.ui.UIUtil;
import com.jetbrains.python.refactoring.PyRefactoringUiService;
import com.jetbrains.python.refactoring.introduce.IntroduceOperation;
import com.jetbrains.python.refactoring.introduce.IntroduceValidator;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class PythonVarPostfixTemplateTest extends BasePlatformTestCase {
    public void testProviderIsRegistered() {
        myFixture.configureByText("example.py", "value");
        assertTrue(LanguagePostfixTemplate.LANG_EP.allForLanguage(myFixture.getFile().getLanguage())
                .stream().anyMatch(PythonVarPostfixTemplateProvider.class::isInstance));
    }

    public void testPostfixKey() {
        myFixture.configureByText("example.py", "get_user().var<caret>");
        assertEquals(".var", new PostfixLiveTemplate().computeTemplateKey(callback()));
    }

    public void testNoTriggerInComment() {
        myFixture.configureByText("example.py", "# get_user().var<caret>");
        assertNull(new PostfixLiveTemplate().computeTemplateKey(callback()));
    }

    public void testNoTriggerInsideString() {
        myFixture.configureByText("example.py", "text = 'hello.var<caret>'");
        assertNull(new PostfixLiveTemplate().computeTemplateKey(callback()));
    }

    public void testExpandInvokesNativeExtraction() {
        myFixture.configureByText("example.py", "get_user().var<caret>");
        new PostfixLiveTemplate().expand(".var", callback());
        UIUtil.dispatchAllInvocationEvents();
        String result = myFixture.getEditor().getDocument().getText();
        assertFalse(result, result.contains(".var"));
        assertTrue(result, result.matches("(?s)\\s*\\w+ = get_user\\(\\)\\s*"));
    }

    public void testTabExpandsPostfix() {
        myFixture.configureByText("example.py", "get_user().var<caret>");
        expandByTab();
        String result = myFixture.getEditor().getDocument().getText();
        assertTrue(result, result.matches("(?s)\\s*\\w+ = get_user\\(\\)\\s*"));
    }

    public void testExtractionFromAssignment() {
        myFixture.configureByText("example.py", "response = client.get_user().var<caret>");
        expandByTab();
        String result = myFixture.getEditor().getDocument().getText();
        assertTrue(result, result.matches("(?s)(\\w+) = client\\.get_user\\(\\)\\s+response = \\1\\s*"));
    }

    public void testExtractionStaysInsideFunction() {
        myFixture.configureByText("example.py", "def example():\n    return client.get_user().var<caret>\n");
        expandByTab();
        String result = myFixture.getEditor().getDocument().getText();
        assertTrue(result, result.matches("(?s)def example\\(\\):\\n    (\\w+) = client\\.get_user\\(\\)\\n    return \\1\\s*"));
    }

    public void testMultilineExtraction() {
        myFixture.configureByText("example.py", "get_user(\n    arg1,\n    arg2,\n).var<caret>");
        expandByTab();
        String result = myFixture.getEditor().getDocument().getText();
        assertFalse(result, result.contains(".var"));
        assertTrue(result, result.matches("(?s)\\w+ = get_user\\(.*arg1,.*arg2,.*\\)\\s*"));
    }

    public void testUndoAndRedo() {
        String original = "get_user().var";
        myFixture.configureByText("example.py", original + "<caret>");
        expandByTab();
        TemplateState state = TemplateManagerImpl.getTemplateState(myFixture.getEditor());
        if (state != null) {
            state.gotoEnd(false);
        }
        String extracted = myFixture.getEditor().getDocument().getText();
        assertFalse(extracted, extracted.contains(".var"));
        myFixture.performEditorAction("$Undo");
        assertEquals(original, myFixture.getEditor().getDocument().getText());
        myFixture.performEditorAction("$Redo");
        assertEquals(extracted, myFixture.getEditor().getDocument().getText());
    }

    public void testNativeNamingUiIsRequested() {
        AtomicReference<IntroduceOperation> request = new AtomicReference<>();
        // The SDK's headless UI service is a no-op. Observe the native UI request
        // without replacing the refactoring engine or its expression processing.
        ServiceContainerUtil.replaceService(ApplicationManager.getApplication(), PyRefactoringUiService.class,
                new PyRefactoringUiService() {
                    @Override
                    public void performInplaceIntroduceVariable(IntroduceOperation operation, PsiElement declaration) {
                        assertFalse("Native naming must run outside a write action",
                                ApplicationManager.getApplication().isWriteAccessAllowed());
                        request.set(operation);
                    }
                }, getTestRootDisposable());
        myFixture.configureByText("example.py", "get_user().var<caret>");
        expandByTab();
        assertNotNull("Native in-place naming must be requested", request.get());
        assertNotNull(request.get().getName());
    }

    public void testNativeDialogNameIsUsed() {
        ServiceContainerUtil.replaceService(ApplicationManager.getApplication(), PyRefactoringUiService.class,
                new PyRefactoringUiService() {
                    @Override
                    public void performIntroduceWithDialog(IntroduceOperation operation, String title,
                                                           IntroduceValidator validator, String helpId,
                                                           Consumer<? super IntroduceOperation> callback) {
                        operation.setName("chosen_user");
                        operation.setReplaceAll(false);
                        callback.accept(operation);
                    }
                }, getTestRootDisposable());
        myFixture.configureByText("example.py", "response = get_user().var<caret>");
        myFixture.getEditor().getSettings().setVariableInplaceRenameEnabled(false);
        expandByTab();
        assertEquals("chosen_user = get_user()\nresponse = chosen_user",
                myFixture.getEditor().getDocument().getText().strip());
    }

    private void expandByTab() {
        myFixture.performEditorAction("ExpandLiveTemplateByTab");
        UIUtil.dispatchAllInvocationEvents();
    }

    private CustomTemplateCallback callback() {
        return new CustomTemplateCallback(myFixture.getEditor(), myFixture.getFile());
    }
}
