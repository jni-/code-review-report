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
    override fun equals(other: Any?): Boolean {
        if (other !is LineComment) {
            return false
        }

        return EqualsBuilder()
            .append(comment, other.comment)
            .append(context.file.path, other.context.file.path)
            .append(context.lineStart, other.context.lineStart)
            .append(context.lineEnd, other.context.lineEnd)
            .isEquals
    }

    override fun hashCode(): Int {
        return HashCodeBuilder().append(comment).append(context.file.path).append(context.lineStart).append(context.lineEnd).toHashCode()
    }

    fun updateText(newText: String) {
        comment = newText
    }

    fun updateIsImportant(isImportant: Boolean) {
        this.isImportant = isImportant
    }
}
