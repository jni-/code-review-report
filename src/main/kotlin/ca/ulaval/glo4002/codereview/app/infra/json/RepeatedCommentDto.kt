package ca.ulaval.glo4002.codereview.app.infra.json

import ca.ulaval.glo4002.codereview.app.model.LineCommentContext
import ca.ulaval.glo4002.codereview.app.model.RepeatedComment
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.nio.file.Path
import java.util.*

data class RepeatedCommentExampleDto(
    val fileRelativePath: String,
    val lineStart: Int,
    val lineEnd: Int,
    val snippet: String
)

data class RepeatedCommentDto(
    val id: String,
    val comment: String,
    val isImportant: Boolean,
    val examples: List<RepeatedCommentExampleDto>,
) {
    companion object {
        fun fromRepeatedComment(project: Project, comment: RepeatedComment): RepeatedCommentDto {
            return RepeatedCommentDto(
                comment.id.toString(),
                comment.comment,
                comment.isImportant,
                comment.examples.map {
                    val fileRelativePath = it.file.path.replace(project.basePath!!, "")
                    RepeatedCommentExampleDto(fileRelativePath, it.lineStart, it.lineEnd, it.snippet)
                }
            )
        }
    }

    fun toRepeatedComment(project: Project): RepeatedComment {
        return RepeatedComment(
            comment,
            isImportant,
            examples.map {
                LineCommentContext(
                    LocalFileSystem.getInstance().findFileByNioFile(Path.of(project.basePath!!, it.fileRelativePath))!!,
                    it.lineStart,
                    it.lineEnd,
                    it.snippet
                )
            }.toMutableList(),
            UUID.fromString(id)
        )
    }
}
