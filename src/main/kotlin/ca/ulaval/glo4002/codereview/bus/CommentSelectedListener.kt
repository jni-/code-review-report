package ca.ulaval.glo4002.codereview.bus

import ca.ulaval.glo4002.codereview.app.model.Review
import com.intellij.util.messages.Topic
import java.util.*

@FunctionalInterface
interface CommentSelectedListener {
    companion object {
        @Topic.ProjectLevel
        val TOPIC: Topic<CommentSelectedListener> = Topic("ca.ulaval.glo4002.codereview.commentSelected", CommentSelectedListener::class.java)
    }

    fun onCommentSelected(review: Review, selectedComment: UUID)
}
