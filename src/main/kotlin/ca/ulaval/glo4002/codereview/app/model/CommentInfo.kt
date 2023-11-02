package ca.ulaval.glo4002.codereview.app.model

import java.util.*

data class CommentInfo(
    val id: UUID,
    val comment: String,
    val context: LineCommentContext,
    val isExample: Boolean
)
