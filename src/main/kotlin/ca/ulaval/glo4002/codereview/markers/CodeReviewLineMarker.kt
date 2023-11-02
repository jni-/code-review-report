package ca.ulaval.glo4002.codereview.markers

import ca.ulaval.glo4002.codereview.app.model.CommentInfo
import ca.ulaval.glo4002.codereview.app.model.Review
import ca.ulaval.glo4002.codereview.app.services.CodeReviewService
import ca.ulaval.glo4002.codereview.bus.CommentSelectedListener
import ca.ulaval.glo4002.codereview.bus.ReviewChangedListener
import ca.ulaval.glo4002.codereview.icons.AllIcons
import ca.ulaval.glo4002.codereview.toolWindow.CodeReviewPreviewWindowFactory
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.plugins.template.MyBundle
import javax.swing.Icon

class CodeReviewLineMarker : LineMarkerProviderDescriptor() {
    private val project = ProjectManager.getInstance().openProjects.getOrElse(0) { null }
    private val service: CodeReviewService? = project?.service<CodeReviewService>()
    private val messageBus = project?.messageBus

    init {
        messageBus?.connect()?.subscribe(ReviewChangedListener.TOPIC, object : ReviewChangedListener {
            override fun onReviewChanged(review: Review) {
                DaemonCodeAnalyzer.getInstance(project).restart()
            }
        })
    }

    override fun getName(): String = MyBundle.message("markers.name")
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null


    override fun collectSlowLineMarkers(
        elements: MutableList<out PsiElement>,
        result: MutableCollection<in LineMarkerInfo<*>>
    ) {
        if (service == null || project == null || messageBus == null) {
            return
        }

        val review = service.getReview()
        elements
            .filterIsInstance<PsiFile>()
            .forEach { element ->
                val comments = review.getLineCommentsForFile(element.containingFile.virtualFile)
                comments.forEach { comment ->
                    val start = element.viewProvider.document.getLineStartOffset(comment.context.lineStart - 1)
                    val end = element.viewProvider.document.getLineEndOffset(comment.context.lineEnd - 1)
                    val marker = ReviewLineMarkerInfo(element, TextRange(start, end), comment) {
                        val instance =
                            ToolWindowManager.getInstance(project).getToolWindow(CodeReviewPreviewWindowFactory.MyToolWindow.ID)
                        instance?.show {
                            messageBus.syncPublisher(CommentSelectedListener.TOPIC).onCommentSelected(review, comment.id)
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
