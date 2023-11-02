package ca.ulaval.glo4002.codereview.app.model

import java.util.*

data class RepeatedComment(
    var comment: String,
    var isImportant: Boolean,
    val examples: MutableList<LineCommentContext>,
    val id: UUID = UUID.randomUUID()
) {
    fun updateText(newText: String) {
        comment = newText
    }

    fun updateIsImportant(isImportant: Boolean) {
        this.isImportant = isImportant
    }

    fun addExample(context: LineCommentContext) {
        examples.add(context)
    }
}
