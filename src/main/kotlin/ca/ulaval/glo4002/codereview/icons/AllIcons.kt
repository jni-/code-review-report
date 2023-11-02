package ca.ulaval.glo4002.codereview.icons

import com.intellij.openapi.util.IconLoader

class AllIcons {
    companion object {
        @JvmField
        val CodeReview = IconLoader.getIcon("/icons/marker.svg", AllIcons::class.java);
        @JvmField
        val CodeReviewToolIcon = IconLoader.getIcon("/icons/marker-tool-icon.svg", AllIcons::class.java);
        @JvmField
        val CodeReviewExample = IconLoader.getIcon("/icons/marker-example.svg", AllIcons::class.java);
    }
}
