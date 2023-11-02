package ca.ulaval.glo4002.codereview.app.model

import com.intellij.openapi.vfs.VirtualFile
import java.util.*

class Review(val id: UUID = UUID.randomUUID()) {
    val lineComments: MutableList<LineComment> = mutableListOf()
    val repeatedComments: MutableList<RepeatedComment> = mutableListOf()
    val generalComments: MutableList<GeneralComment> = mutableListOf()

    fun addLineComment(lineComment: LineComment) {
        lineComments += lineComment
    }

    fun getLineCommentsForFile(virtualFile: VirtualFile?): List<CommentInfo> {
        if (virtualFile == null) {
            return emptyList()
        }

        val lineInfos = lineComments
            .filter { it.context.file == virtualFile }
            .map { CommentInfo(it.id, it.comment, it.context, false) }

        val repeatedInfos = repeatedComments
            .flatMap { repeatedComment ->
                repeatedComment.examples
                    .filter { it.file == virtualFile }
                    .map { CommentInfo(repeatedComment.id, repeatedComment.comment, it, true) }
            }

        return lineInfos + repeatedInfos
    }

    fun makeCommentRepeated(id: UUID) {
        findLineComment(id)?.let(::makeCommentRepeated)
    }

    fun makeCommentRepeated(lineComment: LineComment) {
        lineComments.remove(lineComment)
        addRepeatedExample(lineComment.comment, lineComment.isImportant, lineComment.context)
    }

    private fun findLineComment(id: UUID): LineComment? {
        return lineComments.find { it.id == id }
    }

    fun addRepeatedExample(comment: String, isImportant: Boolean, example: LineCommentContext) {
        val existingComment = repeatedComments.find { it.comment == comment }
        if (existingComment != null) {
            existingComment.examples.add(example)
        } else {
            repeatedComments.add(RepeatedComment(comment, isImportant, mutableListOf(example)))
        }
    }

    fun makeCommentUnrepeated(id: UUID) {
        val repeatedComment = repeatedComments.find { it.id == id }
        if (repeatedComment != null) {
            repeatedComments.remove(repeatedComment)
            repeatedComment.examples.forEach {
                lineComments.add(LineComment(repeatedComment.comment, repeatedComment.isImportant, it))
            }
        }
    }

    fun makeExampleUnrepeated(id: UUID, identifier: LineCommentContextIdentifier) {
        val repeatedComment = repeatedComments.find { it.id == id }
        if (repeatedComment != null) {
            val example = repeatedComment.examples.find { it.has(identifier) }
            if (example != null) {
                repeatedComment.examples.remove(example)
                lineComments.add(LineComment(repeatedComment.comment, repeatedComment.isImportant, example))
                if (repeatedComment.examples.isEmpty()) {
                    repeatedComments.remove(repeatedComment)
                }
            }
        }
    }

    fun removeById(id: UUID) {
        generalComments.removeIf { it.id == id }
        repeatedComments.removeIf { it.id == id }
        lineComments.removeIf { it.id == id }
    }

    fun removeExample(id: UUID, identifier: LineCommentContextIdentifier) {
        val comment = repeatedComments.find { it.id == id }
        if (comment != null) {
            comment.examples.removeIf { it.has(identifier) }
            if (comment.examples.isEmpty()) {
                repeatedComments.remove(comment)
            }
        }
    }

    fun editCommentText(id: UUID, newText: String, isImportant: Boolean) {
        generalComments.find { it.id == id }?.updateText(newText)
        generalComments.find { it.id == id }?.updateIsImportant(isImportant)
        repeatedComments.find { it.id == id }?.updateText(newText)
        repeatedComments.find { it.id == id }?.updateIsImportant(isImportant)
        lineComments.find { it.id == id }?.updateText(newText)
        lineComments.find { it.id == id }?.updateIsImportant(isImportant)
    }

    fun getCommentText(id: UUID): EditableComment {
        val text = generalComments.find { it.id == id }?.comment ?: repeatedComments.find { it.id == id }?.comment
        ?: lineComments.find { it.id == id }?.comment ?: ""

        val isImportant = generalComments.find { it.id == id }?.isImportant ?: repeatedComments.find { it.id == id }?.isImportant
        ?: lineComments.find { it.id == id }?.isImportant ?: false

        return EditableComment(text, isImportant)
    }

    fun addGeneralComment(text: String, isImportant: Boolean) {
        generalComments += GeneralComment(text, isImportant)
    }

    fun addExampleComment(repeatedCommentId: UUID, context: LineCommentContext) {
        repeatedComments.find { it.id == repeatedCommentId }?.addExample(context)
    }
}
