package ca.ulaval.glo4002.codereview.app.infra.json

import ca.ulaval.glo4002.codereview.app.model.LineComment
import ca.ulaval.glo4002.codereview.app.model.LineCommentContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.nio.file.Path
import java.util.UUID

data class LineCommentDto(
    val id: String,
    val comment: String,
    val isImportant: Boolean,
    val fileRelativePath: String,
    val lineStart: Int,
    val lineEnd: Int,
    val snippet: String
) {
    companion object {
        fun fromLineComment(project: Project, comment: LineComment): LineCommentDto {
            val fileRelativePath = comment.context.file.path.replace(project.basePath!!, "")
            return LineCommentDto(
                comment.id.toString(),
                comment.comment,
                comment.isImportant,
                fileRelativePath,
                comment.context.lineStart,
                comment.context.lineEnd,
                comment.context.snippet
            )
        }
    }

    fun toLineComment(project: Project): LineComment {
        return LineComment(
            comment,
            isImportant,
            LineCommentContext(
                LocalFileSystem.getInstance().findFileByNioFile(Path.of(project.basePath!!, fileRelativePath))!!,
                lineStart,
                lineEnd,
                snippet
            ),
            UUID.fromString(id)
        )
    }
}
