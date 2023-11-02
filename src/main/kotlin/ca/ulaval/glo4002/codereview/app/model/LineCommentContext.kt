package ca.ulaval.glo4002.codereview.app.model

import com.intellij.openapi.vfs.VirtualFile

data class LineCommentContext(
    val file: VirtualFile,
    val lineStart: Int,
    val lineEnd: Int,
    val snippet: String,
) {
    companion object {
        const val SNIPPET_RANGE_AROUND = 10
    }

    val lineRange: String
        get(): String = if (lineStart != lineEnd) {
            "${lineStart}-${lineEnd}"
        } else {
            lineStart.toString()
        }

    val snippetFirstLine: Int
        get(): Int = lineStart - SNIPPET_RANGE_AROUND

    fun toIdentifier(): LineCommentContextIdentifier {
        return LineCommentContextIdentifier(file.path, lineStart, lineEnd)
    }

    fun has(identifier: LineCommentContextIdentifier): Boolean {
        return toIdentifier() == identifier
    }
}
