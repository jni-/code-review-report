package ca.ulaval.glo4002.codereview.app.infra.code

import ca.ulaval.glo4002.codereview.app.model.LineCommentContext
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange

class LineContextExtractor {
    companion object {
        fun extractFromCursor(project: Project?): LineCommentContext? {
            if (project != null) {
                val editor = FileEditorManager.getInstance(project).selectedTextEditor
                if (editor != null) {
                    val document = editor.document
                    val file = FileDocumentManager.getInstance().getFile(document)
                    if (file != null) {
                        val caret = editor.caretModel.primaryCaret
                        val start = editor.visualToLogicalPosition(caret.selectionStartPosition).line + 1
                        val end = editor.visualToLogicalPosition(caret.selectionEndPosition).line + 1
                        val snippet = getSnippet(document, start, end)

                        return LineCommentContext(file, start, end, snippet)
                    }
                }
            }

            return null
        }

        private fun getSnippet(document: Document, logicalStartLine: Int, logicalEndLine: Int): String {
            val snippetStart = (logicalStartLine - 1 - LineCommentContext.SNIPPET_RANGE_AROUND)
                .coerceAtLeast(0)
                .let(document::getLineStartOffset)
            val snippetEnd = (logicalEndLine - 1 + LineCommentContext.SNIPPET_RANGE_AROUND)
                .coerceAtMost(document.lineCount - 1)
                .let(document::getLineEndOffset)

            return TextRange.from(snippetStart, snippetEnd - snippetStart)
                .let(document::getText)
                .let(::prettifySnippet)
        }

        private fun prettifySnippet(text: String): String {
            return text.lines()
                .joinToString("\n")
                .trimIndent()
        }
    }
}
