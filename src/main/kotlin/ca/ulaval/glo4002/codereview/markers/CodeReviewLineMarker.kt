package ca.ulaval.glo4002.codereview.markers

import ca.ulaval.glo4002.codereview.app.model.CommentInfo
import ca.ulaval.glo4002.codereview.app.services.CodeReviewService
import ca.ulaval.glo4002.codereview.bus.CommentSelectedListener
import ca.ulaval.glo4002.codereview.icons.AllIcons
import ca.ulaval.glo4002.codereview.toolWindow.CodeReviewPreviewWindowFactory
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import ca.ulaval.glo4002.codereview.MyBundle
import javax.swing.Icon

class CodeReviewLineMarker : LineMarkerProviderDescriptor() {

    override fun getName(): String = MyBundle.message("markers.name")
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null


    override fun collectSlowLineMarkers(
        elements: MutableList<out PsiElement>,
        result: MutableCollection<in LineMarkerInfo<*>>
    ) {
        val project = elements.firstOrNull()?.project ?: return
        val service = project.service<CodeReviewService>()

        val review = service.getReview()
        elements
            .filterIsInstance<PsiFile>()
            .forEach { element ->
                val document = element.viewProvider.document ?: return@forEach
                val comments = review.getLineCommentsForFile(element.containingFile.virtualFile)
                comments.forEach { comment ->
                    // Stale reviews can reference lines that no longer exist in the file
                    if (comment.context.lineStart < 1 || comment.context.lineEnd > document.lineCount) {
                        return@forEach
                    }
                    val start = document.getLineStartOffset(comment.context.lineStart - 1)
                    val end = document.getLineEndOffset(comment.context.lineEnd - 1)
                    val marker = ReviewLineMarkerInfo(element, TextRange(start, end), comment) {
                        val instance =
                            ToolWindowManager.getInstance(project).getToolWindow(CodeReviewPreviewWindowFactory.MyToolWindow.ID)
                        instance?.show {
                            project.messageBus.syncPublisher(CommentSelectedListener.TOPIC).onCommentSelected(review, comment.id)
                        }
                    }
                    result.add(marker)
                }
            }
    }

}

class ReviewLineMarkerInfo(element: PsiElement, range: TextRange, private val comment: CommentInfo, onClick: () -> Unit) :
    MergeableLineMarkerInfo<PsiElement>(
        element,
        range,
        if (comment.isExample) AllIcons.CodeReviewExample else AllIcons.CodeReview,
        { comment.comment },
        { _ -> comment.comment },
        { _, _ ->
            onClick()
        },
        GutterIconRenderer.Alignment.LEFT,
        { MyBundle.message("markers.accessibleName", comment.comment) }
    ) {
    override fun canMergeWith(info: MergeableLineMarkerInfo<*>): Boolean {
        return info is ReviewLineMarkerInfo
    }

    override fun getCommonIcon(infos: MutableList<out MergeableLineMarkerInfo<*>>): Icon  {
        if (infos.all { it is ReviewLineMarkerInfo}) {
            return if (infos.all { (it as ReviewLineMarkerInfo).comment.isExample }) {
                AllIcons.CodeReviewExample
            } else {
                AllIcons.CodeReview
            }
        }

        return AllIcons.CodeReview
    }
}
