package ca.ulaval.glo4002.codereview.bus

import ca.ulaval.glo4002.codereview.app.model.Review
import com.intellij.util.messages.Topic

@FunctionalInterface
interface ReviewChangedListener {
    companion object {
        @Topic.ProjectLevel
        val TOPIC: Topic<ReviewChangedListener> =
            Topic("ca.ulaval.glo4002.codereview.reviewChanged", ReviewChangedListener::class.java)
    }

    fun onReviewChanged(review: Review)
}
