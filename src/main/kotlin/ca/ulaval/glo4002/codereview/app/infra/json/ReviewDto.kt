package ca.ulaval.glo4002.codereview.app.infra.json

import ca.ulaval.glo4002.codereview.app.model.GeneralComment
import ca.ulaval.glo4002.codereview.app.model.Review
import com.intellij.openapi.project.Project
import java.util.*

data class ReviewDto(
    val id: String,
    val lineComments: List<LineCommentDto>,
    val repeatedComments: List<RepeatedCommentDto>,
    val generalComments: List<GeneralCommentDto>
) {
    companion object {
        fun fromReview(project: Project, review: Review): ReviewDto {
            return ReviewDto(
                review.id.toString(),
                review.lineComments.map { LineCommentDto.fromLineComment(project, it) },
                review.repeatedComments.map { RepeatedCommentDto.fromRepeatedComment(project, it) },
                review.generalComments.map { GeneralCommentDto(it.id.toString(), it.comment, it.isImportant) }
            )
        }
    }

    fun toReview(project: Project): Review {
        val review = Review(UUID.fromString(id))
        review.lineComments.addAll(lineComments.map { it.toLineComment(project) })
        review.repeatedComments.addAll(repeatedComments.map { it.toRepeatedComment(project) })
        review.generalComments.addAll(generalComments.map { GeneralComment(it.comment, it.isImportant, UUID.fromString(it.id)) })
        return review
    }
}
