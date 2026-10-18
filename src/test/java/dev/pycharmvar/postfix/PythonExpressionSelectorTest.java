package dev.pycharmvar.postfix;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.List;

public final class PythonExpressionSelectorTest extends BasePlatformTestCase {
    public void testFunctionCall() {
        assertEquals(List.of("get_user()"), candidates("get_user()<caret>"));
    }

    public void testAssignmentValue() {
        assertEquals(List.of("client.get_user()"), candidates("response = client.get_user()<caret>"));
    }

    public void testChainedAttribute() {
        assertEquals(List.of("items[index].name"), candidates("items[index].name<caret>"));
    }

    public void testBinaryExpressionOffersBothCandidates() {
        assertEquals(List.of("b", "a + b"), candidates("a + b<caret>"));
    }

    public void testMultilineCall() {
        assertEquals(List.of("some_function(\n    arg1,\n    arg2,\n)"),
                candidates("some_function(\n    arg1,\n    arg2,\n)<caret>"));
    }

    public void testStringContentIsNotExpression() {
        assertEmpty(candidates("text = 'hello<caret>'"));
    }

    public void testCommentIsNotExpression() {
        assertEmpty(candidates("# get_user()<caret>"));
    }

    public void testNoExpressionAtStartOfFile() {
        assertEmpty(candidates("<caret>"));
    }

    public void testInvalidExpressionIsRejected() {
        assertEmpty(candidates("get_user(,<caret>"));
    }

    public void testOtherLanguagesAreRejected() {
        myFixture.configureByText("example.txt", "value");
        assertEmpty(PythonExpressionResolver.findExpressions(myFixture.getFile(), 5));
    }

    public void testAssignmentTargetIsRejected() {
        assertEmpty(candidates("target<caret> = value"));
    }

    public void testImportIsRejected() {
        assertEmpty(candidates("import package.module<caret>"));
    }

    public void testCompleteStringLiteralCanBeExtracted() {
        assertEquals(List.of("'hello'"), candidates("'hello'<caret>"));
    }

    public void testCallArgumentCanBeExtracted() {
        assertEquals(List.of("get_user()"), candidates("consume(get_user()<caret>)"));
    }

    private List<String> candidates(String source) {
        myFixture.configureByText("example.py", source);
        Document document = myFixture.getEditor().getDocument();
        PsiDocumentManager.getInstance(getProject()).commitDocument(document);
        int offset = myFixture.getCaretOffset();
        return new PythonExpressionSelector()
                .getExpressions(myFixture.getFile(), document, offset)
                .stream().map(PsiElement::getText).toList();
    }
}
