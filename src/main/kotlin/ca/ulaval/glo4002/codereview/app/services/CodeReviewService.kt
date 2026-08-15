package ca.ulaval.glo4002.codereview.app.services

import ca.ulaval.glo4002.codereview.app.infra.json.ReviewJsonRepository
import ca.ulaval.glo4002.codereview.app.model.*
import ca.ulaval.glo4002.codereview.bus.ReviewChangedListener
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.util.*

@Service(Service.Level.PROJECT)
class CodeReviewService(val project: Project) : Disposable {
    private val reviewRepository = project.service<ReviewJsonRepository>()

    init {
        // Re-run highlighting so gutter markers reflect the latest review; the connection
        // is disposed with this service, i.e. when the project closes.
        project.messageBus.connect(this).subscribe(ReviewChangedListener.TOPIC, object : ReviewChangedListener {
            override fun onReviewChanged(review: Review) {
                if (!project.isDisposed) {
                    DaemonCodeAnalyzer.getInstance(project).restart("Code review changed")
                }
            }
        })
    }

    override fun dispose() {}

    fun addComment(lineComment: LineComment) {
        val review = reviewRepository.getReview()
        review.addLineComment(lineComment)
        reviewRepository.save(review)
    }

    fun makeCommentRepeated(lineCommentId: UUID) {
        val review = reviewRepository.getReview()
        review.makeCommentRepeated(lineCommentId)
        reviewRepository.save(review)
    }

    fun makeCommentUnrepeated(id: UUID) {
        val review = reviewRepository.getReview()
        review.makeCommentUnrepeated(id)
        reviewRepository.save(review)
    }

    fun reload() {
        reviewRepository.reload()
    }

    fun getReview(): Review {
        return reviewRepository.getReview()
    }

    fun makeExampleUnrepeated(id: UUID, identifier: LineCommentContextIdentifier) {
        val review = reviewRepository.getReview()
        review.makeExampleUnrepeated(id, identifier)
        reviewRepository.save(review)
    }

    fun removeCommentById(id: UUID) {
        val review = reviewRepository.getReview()
        review.removeById(id)
        reviewRepository.save(review)
    }

    fun removeExample(id: UUID, identifier: LineCommentContextIdentifier) {
        val review = reviewRepository.getReview()
        review.removeExample(id, identifier)
        reviewRepository.save(review)
    }

    fun editCommentText(id: UUID, newText: String, isImportant: Boolean) {
        val review = reviewRepository.getReview()
        review.editCommentText(id, newText, isImportant)
        reviewRepository.save(review)
    }

    fun getCommentText(id: UUID): EditableComment {
        val review = reviewRepository.getReview()
        return review.getCommentText(id)
    }

    fun addGeneralComment(text: String, isImportant: Boolean) {
        val review = reviewRepository.getReview()
        review.addGeneralComment(text, isImportant)
        reviewRepository.save(review)
    }

    fun addExampleComment(repeatedCommentId: UUID, context: LineCommentContext) {
        val review = reviewRepository.getReview()
        review.addExampleComment(repeatedCommentId, context)
        reviewRepository.save(review)
    }
}
