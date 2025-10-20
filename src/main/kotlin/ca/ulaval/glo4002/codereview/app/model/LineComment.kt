package ca.ulaval.glo4002.codereview.app.model

import org.apache.commons.lang.builder.EqualsBuilder
import org.apache.commons.lang.builder.HashCodeBuilder
import java.util.*

data class LineComment(
    var comment: String,
    var isImportant: Boolean,
    val context: LineCommentContext,
    val id: UUID = UUID.randomUUID()
) {

    fun updateText(newText: String) {
        comment = newText
    }

    fun updateIsImportant(isImportant: Boolean) {
        this.isImportant = isImportant
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LineComment) return false

        if (comment != other.comment) return false
        if (context.file.path != other.context.file.path) return false
        if (context.lineStart != other.context.lineStart) return false
        if (context.lineEnd != other.context.lineEnd) return false

        return true
    }

    override fun hashCode(): Int {
        var result = context.file.path.hashCode()
        result = 31 * result + comment.hashCode()
        result = 31 * result + context.lineStart.hashCode()
        result = 31 * result + context.lineEnd.hashCode()
        return result
    }
}
