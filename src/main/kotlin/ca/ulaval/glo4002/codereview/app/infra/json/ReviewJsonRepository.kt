package ca.ulaval.glo4002.codereview.app.infra.json

import ca.ulaval.glo4002.codereview.app.model.Review
import ca.ulaval.glo4002.codereview.bus.ReviewChangedListener
import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.messages.MessageBus
import java.nio.file.Path

private const val FILE_NAME = ".review.json"

@Service(Service.Level.PROJECT)
class ReviewJsonRepository(
    private val project: Project,
) : CommentsRepository {
    private val messageBus: MessageBus = project.messageBus
    private var mapper: ObjectMapper = configuredMapper
    private lateinit var review: Review

    override fun getReview(): Review {
        if (this::review.isInitialized.not()) {
            reload()
        }
        return review
    }

    override fun save(review: Review) {
        this.review = review

        ApplicationManager.getApplication().runWriteAction {
            val file = getOrCreateReviewFile()
            val doc = FileDocumentManager.getInstance().getDocument(file)!!

            val dto = ReviewDto.fromReview(project, review)
            doc.setText(mapper.writeValueAsString(dto))
        }

        messageBus.syncPublisher(ReviewChangedListener.TOPIC).onReviewChanged(review)
    }

    override fun reload() {
        val file = getReviewFile()

        review = if (file != null && file.exists()) {
            val doc = FileDocumentManager.getInstance().getDocument(file)!!

            if (doc.text.isNotEmpty()) {
                mapper.readValue(doc.text, ReviewDto::class.java).toReview(project)
            } else {
                Review()
            }
        } else {
            Review()
        }

        messageBus.syncPublisher(ReviewChangedListener.TOPIC).onReviewChanged(review)
    }

    private fun getReviewFile(): VirtualFile? = LocalFileSystem.getInstance()
        .findFileByNioFile(Path.of(project.basePath!!))!!
        .findChild(FILE_NAME)

    private fun getOrCreateReviewFile(): VirtualFile = LocalFileSystem.getInstance()
        .findFileByNioFile(Path.of(project.basePath!!))!!
        .findOrCreateChildData(this, FILE_NAME)
}

interface CommentsRepository {
    fun save(review: Review)
    fun getReview(): Review
    fun reload()
}
